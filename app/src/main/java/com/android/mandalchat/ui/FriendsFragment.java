package com.android.mandalchat.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mandalchat.service.Config;
import com.android.mandalchat.util.CommanClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.android.mandalchat.R;
import com.android.mandalchat.data.FriendDB;
import com.android.mandalchat.data.StaticConfig;
import com.android.mandalchat.model.Friend;
import com.android.mandalchat.model.ListFriend;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerListFrends;
    public static ListFriendsAdapter adapter;
    public FragFriendClickFloatButton onClickFloatButton;
    public static ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static int ACTION_START_CHAT = 1;

    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";

    private BroadcastReceiver deleteFriendReceiver;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    ImageView fab;
    LovelyProgressDialog dialogWait;

    SearchView searchView;


    public FriendsFragment() {
        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (dataListFriend == null) {

            dataListFriend = FriendDB.getInstance(getContext()).getListFriend();
            if (dataListFriend.getListFriend().size() > 0) {
                listFriendID = new ArrayList<>();
                for (Friend friend : dataListFriend.getListFriend()) {
                    listFriendID.add(friend.id);
                }

            }
        }
        View layout = inflater.inflate(R.layout.fragment_people, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        searchView = (SearchView) layout.findViewById(R.id.searchView);
        fab = (ImageView) layout.findViewById(R.id.fab);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        FriendDB.getInstance(getContext()).dropDB();

        adapter = new ListFriendsAdapter(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);

        dialogFindAllFriend = new LovelyProgressDialog(getContext());
        if (listFriendID == null) {
            listFriendID = new ArrayList<>();
            dialogFindAllFriend.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Buddy info on the way...")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if (idDeleted.equals(friend.id)) {
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        dialogWait = new LovelyProgressDialog(getActivity());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LovelyTextInputDialog(v.getContext(), R.style.EditTextTintTheme)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle("Add Buddy")
                        .setMessage("Enter Buddy email")
                        .setIcon(R.drawable.ic_add_friend)
                        .setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .setInputFilter("Email not found", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                Pattern VALID_EMAIL_ADDRESS_REGEX =
                                        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(text);
                                return matcher.find();
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                //Tim id user id
                                findIDEmail(text);
                                //Check xem da ton tai ban ghi friend chua
                                //Ghi them 1 ban ghi
                            }
                        })
                        .show();
            }
        });


        fcmNotification();

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    private void findIDEmail(String email) {
        dialogWait.setCancelable(false)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle("Finding Buddy...")
                .setTopColorRes(R.color.colorPrimary)
                .show();
        FirebaseDatabase.getInstance().getReference().child("user").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialogWait.dismiss();
                if (dataSnapshot.getValue() == null) {
                    //email not found
                    new LovelyInfoDialog(getActivity())
                            .setTopColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_add_friend)
                            .setTitle("Opps! Try again ")
                            .setMessage("Email not found")
                            .show();
                } else {
                    String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                    if (id.equals(StaticConfig.UID)) {
                        new LovelyInfoDialog(getActivity())
                                .setTopColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_add_friend)
                                .setTitle("Opps! Try again")
                                .setMessage("Email not valid")
                                .show();
                    } else {
                        HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                        Friend user = new Friend();
                        user.name = (String) userMap.get("name");
                        user.email = (String) userMap.get("email");
                        user.avata = (String) userMap.get("avata");
                        user.tokenn = (String) userMap.get("not_token");
                        user.joining = (String) userMap.get("joining");
                        user.profession = (String) userMap.get("profession");
                        user.mobile = (String) userMap.get("mobile");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        checkBeforAddFriend(id, user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Lay danh sach friend cua một UID
     */
    private void checkBeforAddFriend(final String idFriend, Friend userInfo) {
        dialogWait.setCancelable(false)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle("Add Buddy")
                .setTopColorRes(R.color.colorPrimary)
                .show();

        //Check xem da ton tai id trong danh sach id chua
        if (listFriendID.contains(idFriend)) {
            dialogWait.dismiss();
            new LovelyInfoDialog(getActivity())
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Buddy")
                    .setMessage("User " + userInfo.email + " has been friend")
                    .show();
        } else {
            addFriend(idFriend, true);
            listFriendID.add(idFriend);
            dataListFriend.getListFriend().add(userInfo);
            FriendDB.getInstance(getContext()).addFriend(userInfo);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Add friend
     *
     * @param idFriend
     */
    private void addFriend(final String idFriend, boolean isIdFriend) {
        if (idFriend != null) {
            if (isIdFriend) {
                FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(idFriend, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogWait.dismiss();
                                new LovelyInfoDialog(getActivity())
                                        .setTopColorRes(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_add_friend)
                                        .setTitle("Oops")
                                        .setMessage("Failed to add buddy")
                                        .show();
                            }
                        });
            } else {
                FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            addFriend(null, false);
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogWait.dismiss();
                                new LovelyInfoDialog(getActivity())
                                        .setTopColorRes(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_add_friend)
                                        .setTitle("Oops")
                                        .setMessage("Failed to add buddy")
                                        .show();
                            }
                        });
            }
        } else {
            dialogWait.dismiss();
            new LovelyInfoDialog(getActivity())
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Wow")
                    .setMessage("Your buddy added successfully")
                    .show();
        }
    }



    private void fcmNotification() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    //displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");


                }
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter.mapMark != null) {
            ListFriendsAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriend.getListFriend().clear();
        adapter.notifyDataSetChanged();
        FriendDB.getInstance(getContext()).dropDB();

        getListFriendUId();
    }

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;
        LovelyProgressDialog dialogWait;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            dialogWait = new LovelyProgressDialog(context);
            return this;
        }

        @Override
        public void onClick(final View view) {
            new LovelyTextInputDialog(view.getContext(), R.style.EditTextTintTheme)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Add Buddy")
                    .setMessage("Enter Buddy email")
                    .setIcon(R.drawable.ic_add_friend)
                    .setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    .setInputFilter("Email not found", new LovelyTextInputDialog.TextFilter() {
                        @Override
                        public boolean check(String text) {
                            Pattern VALID_EMAIL_ADDRESS_REGEX =
                                    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(text);
                            return matcher.find();
                        }
                    })
                    .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                        @Override
                        public void onTextInputConfirmed(String text) {
                            //Tim id user id
                            findIDEmail(text);
                            //Check xem da ton tai ban ghi friend chua
                            //Ghi them 1 ban ghi
                        }
                    })
                    .show();
        }

        /**
         * TIm id cua email tren server
         *
         * @param email
         */
        private void findIDEmail(String email) {
            dialogWait.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Finding Buddy...")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            FirebaseDatabase.getInstance().getReference().child("user").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialogWait.dismiss();
                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        new LovelyInfoDialog(context)
                                .setTopColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_add_friend)
                                .setTitle("Opps! Try again ")
                                .setMessage("Email not found")
                                .show();
                    } else {
                        String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        if (id.equals(StaticConfig.UID)) {
                            new LovelyInfoDialog(context)
                                    .setTopColorRes(R.color.colorAccent)
                                    .setIcon(R.drawable.ic_add_friend)
                                    .setTitle("Opps! Try again")
                                    .setMessage("Email not valid")
                                    .show();
                        } else {
                            HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                            Friend user = new Friend();
                            user.name = (String) userMap.get("name");
                            user.email = (String) userMap.get("email");
                            user.avata = (String) userMap.get("avata");
                            user.tokenn = (String) userMap.get("not_token");
                            user.joining = (String) userMap.get("joining");
                            user.profession = (String) userMap.get("profession");
                            user.mobile = (String) userMap.get("mobile");
                            user.id = id;
                            user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                            checkBeforAddFriend(id, user);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        /**
         * Lay danh sach friend cua một UID
         */
        private void checkBeforAddFriend(final String idFriend, Friend userInfo) {
            dialogWait.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Add Buddy")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();

            //Check xem da ton tai id trong danh sach id chua
            if (listFriendID.contains(idFriend)) {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("Buddy")
                        .setMessage("User " + userInfo.email + " has been friend")
                        .show();
            } else {
                addFriend(idFriend, true);
                listFriendID.add(idFriend);
                dataListFriend.getListFriend().add(userInfo);
                FriendDB.getInstance(getContext()).addFriend(userInfo);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * Add friend
         *
         * @param idFriend
         */
        private void addFriend(final String idFriend, boolean isIdFriend) {
            if (idFriend != null) {
                if (isIdFriend) {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(idFriend, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("Oops")
                                            .setMessage("Failed to add buddy")
                                            .show();
                                }
                            });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFriend(null, false);
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("Oops")
                                            .setMessage("Failed to add buddy")
                                            .show();
                                }
                            });
                }
            } else {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("Wow")
                        .setMessage("Your buddy added successfully")
                        .show();
            }
        }


    }

    /**
     * Lay danh sach ban be tren server
     */
    private void getListFriendUId() {
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        listFriendID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);
                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Truy cap bang user lay thong tin id nguoi dung
     */
    private void getAllFriendInfo(final int index) {
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);

        } else {
            final String id = listFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.tokenn = (String) mapUserInfo.get("not_token");
                        user.joining = (String) mapUserInfo.get("joining");
                        user.profession = (String) mapUserInfo.get("profession");
                        user.mobile = (String) mapUserInfo.get("mobile");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataListFriend.getListFriend().add(user);
                        FriendDB.getInstance(getContext()).addFriend(user);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

class ListFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ListFriend listFriend;



    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private FriendsFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myGameRef;

    CommanClass cc;

    public Dialog dialog;

    TextView tv_send_money, tv_close, tv_1, tv_2, tv_3, tv_4, tv_5, tv_6;
    EditText ed_message;
    LinearLayout clear_text;

    public ListFriendsAdapter(Context context, ListFriend listFriend, FriendsFragment fragment) {
        this.listFriend = listFriend;

        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();

        cc = new CommanClass(context);

        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false);
        return new ItemFriendViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).avata;
        final String tokken = listFriend.getListFriend().get(position).tokenn;
        final String joining = listFriend.getListFriend().get(position).joining;
        final String profession = listFriend.getListFriend().get(position).profession;
        final String mobile = listFriend.getListFriend().get(position).mobile;
        ((ItemFriendViewHolder) holder).txtName.setText(name);
        ((ItemFriendViewHolder) holder).txtJoining.setText("Joining : " + " " + joining);
        ((ItemFriendViewHolder) holder).txtProfession.setText(profession);


        myGameRef = database.getReference("user").child(id);

        ((ItemFriendViewHolder) holder).ln_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.e("@@@FinalUserToken", tokken);
                Log.e("@@@FinalUserJOINING", joining);
                Log.e("@@@FinalUserPROFESSION", profession);
                Log.e("@@@FinalUserMOBILE", mobile);

                myGameRef.child("alertEmploye").child("alert").setValue(true);

                message_dialog(tokken);

                Log.e("@@Employee Id", id);

            }
        });

        ((ItemFriendViewHolder) holder).ln_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+mobile));
                context.startActivity(callIntent);


            }
        });

        ((ItemFriendViewHolder) holder).ln_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("token_not",tokken);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
                ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                idFriend.add(id);
                intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                ChatActivity.bitmapAvataFriend = new HashMap<>();
                if (!avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                    byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                } else {
                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                }

                mapMark.put(id, null);
                fragment.startActivityForResult(intent, FriendsFragment.ACTION_START_CHAT);

            }
        });

        ((ItemFriendViewHolder) holder).ln_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String friendName = (String)((ItemFriendViewHolder) holder).txtName.getText();

                new AlertDialog.Builder(context)
                        .setTitle("Delete Buddy")
                        .setMessage("Are you sure want to delete "+friendName+ "?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                final String idFriendRemoval = listFriend.getListFriend().get(position).id;
                                dialogWaitDeleting.setTitle("Please wait...")
                                        .setCancelable(false)
                                        .setTopColorRes(R.color.colorAccent)
                                        .show();
                                deleteFriend(idFriendRemoval);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

            }
        });


        if (listFriend.getListFriend().get(position).message.text.length() > 0) {

            if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {


            } else {


            }
            String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
            String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));


        } else {

            if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
                mapQuery.put(id, FirebaseDatabase.getInstance().getReference().child("message/" + idRoom).limitToLast(1));
                mapChildListener.put(id, new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();

                        if (listFriend.getListFriend().isEmpty()){

                        }else {

                            if (mapMark.get(id) != null) {
                                if (!mapMark.get(id)) {
                                    listFriend.getListFriend().get(position).message.text = id + mapMessage.get("text");
                                } else {
                                    listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                }

                                notifyDataSetChanged();
                                mapMark.put(id, false);
                            } else {
                                listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                notifyDataSetChanged();
                            }
                            listFriend.getListFriend().get(position).message.timestamp = (long) mapMessage.get("timestamp");
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                mapMark.put(id, true);
            } else {
                mapQuery.get(id).removeEventListener(mapChildListener.get(id));
                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                mapMark.put(id, true);
            }
        }
        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ((ItemFriendViewHolder) holder).avata.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ItemFriendViewHolder) holder).avata.setImageBitmap(src);
        }


        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id+"/status"));
            mapChildListenerOnline.put(id, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if (listFriend.getListFriend().isEmpty()){

                    }else {

                        if(dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment add " + id,  (boolean)dataSnapshot.getValue() +"");
                            listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                            notifyDataSetChanged();
                        }

                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    if (listFriend.getListFriend().isEmpty()){

                    }else {

                        if(dataSnapshot.getValue() != null&& dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment change " + id,  (boolean)dataSnapshot.getValue() +"");
                            listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                            notifyDataSetChanged();
                        }

                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
        }

        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(10);
        } else {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(0);
        }
    }

    private void message_dialog(final String tokken) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.msg_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dialog.getWindow().setLayout((6 * width) / 7, ActionBar.LayoutParams.WRAP_CONTENT);


        ed_message = (EditText)dialog.findViewById(R.id.ed_message);
        tv_send_money = (TextView)dialog.findViewById(R.id.tv_send_money);
        tv_close = (TextView)dialog.findViewById(R.id.tv_close);

        clear_text = (LinearLayout) dialog.findViewById(R.id.clear_text);

        tv_1 = (TextView)dialog.findViewById(R.id.tv_1);
        tv_2 = (TextView)dialog.findViewById(R.id.tv_2);
        tv_3 = (TextView)dialog.findViewById(R.id.tv_3);
        tv_4 = (TextView)dialog.findViewById(R.id.tv_4);
        tv_5 = (TextView)dialog.findViewById(R.id.tv_5);
        tv_6 = (TextView)dialog.findViewById(R.id.tv_6);

        clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText("");
            }
        });

        tv_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_1.getText().toString());

                tv_1.setTextColor(Color.parseColor("#ffffff"));
                tv_2.setTextColor(Color.parseColor("#000000"));
                tv_3.setTextColor(Color.parseColor("#000000"));
                tv_4.setTextColor(Color.parseColor("#000000"));
                tv_5.setTextColor(Color.parseColor("#000000"));
                tv_6.setTextColor(Color.parseColor("#000000"));

                tv_1.setBackgroundResource(R.drawable.only_line_back);
                tv_2.setBackgroundResource(R.drawable.only_line);
                tv_3.setBackgroundResource(R.drawable.only_line);
                tv_4.setBackgroundResource(R.drawable.only_line);
                tv_5.setBackgroundResource(R.drawable.only_line);
                tv_6.setBackgroundResource(R.drawable.only_line);
            }
        });

        tv_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_2.getText().toString());

                tv_1.setTextColor(Color.parseColor("#000000"));
                tv_2.setTextColor(Color.parseColor("#ffffff"));
                tv_3.setTextColor(Color.parseColor("#000000"));
                tv_4.setTextColor(Color.parseColor("#000000"));
                tv_5.setTextColor(Color.parseColor("#000000"));
                tv_6.setTextColor(Color.parseColor("#000000"));

                tv_1.setBackgroundResource(R.drawable.only_line);
                tv_2.setBackgroundResource(R.drawable.only_line_back);
                tv_3.setBackgroundResource(R.drawable.only_line);
                tv_4.setBackgroundResource(R.drawable.only_line);
                tv_5.setBackgroundResource(R.drawable.only_line);
                tv_6.setBackgroundResource(R.drawable.only_line);
            }
        });


        tv_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_3.getText().toString());

                tv_1.setTextColor(Color.parseColor("#000000"));
                tv_2.setTextColor(Color.parseColor("#000000"));
                tv_3.setTextColor(Color.parseColor("#ffffff"));
                tv_4.setTextColor(Color.parseColor("#000000"));
                tv_5.setTextColor(Color.parseColor("#000000"));
                tv_6.setTextColor(Color.parseColor("#000000"));

                tv_1.setBackgroundResource(R.drawable.only_line);
                tv_2.setBackgroundResource(R.drawable.only_line);
                tv_3.setBackgroundResource(R.drawable.only_line_back);
                tv_4.setBackgroundResource(R.drawable.only_line);
                tv_5.setBackgroundResource(R.drawable.only_line);
                tv_6.setBackgroundResource(R.drawable.only_line);

            }
        });


        tv_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_4.getText().toString());

                tv_1.setTextColor(Color.parseColor("#000000"));
                tv_2.setTextColor(Color.parseColor("#000000"));
                tv_3.setTextColor(Color.parseColor("#000000"));
                tv_4.setTextColor(Color.parseColor("#ffffff"));
                tv_5.setTextColor(Color.parseColor("#000000"));
                tv_6.setTextColor(Color.parseColor("#000000"));

                tv_1.setBackgroundResource(R.drawable.only_line);
                tv_2.setBackgroundResource(R.drawable.only_line);
                tv_3.setBackgroundResource(R.drawable.only_line);
                tv_4.setBackgroundResource(R.drawable.only_line_back);
                tv_5.setBackgroundResource(R.drawable.only_line);
                tv_6.setBackgroundResource(R.drawable.only_line);
            }
        });


        tv_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_5.getText().toString());

                tv_1.setTextColor(Color.parseColor("#000000"));
                tv_2.setTextColor(Color.parseColor("#000000"));
                tv_3.setTextColor(Color.parseColor("#000000"));
                tv_4.setTextColor(Color.parseColor("#000000"));
                tv_5.setTextColor(Color.parseColor("#ffffff"));
                tv_6.setTextColor(Color.parseColor("#000000"));

                tv_1.setBackgroundResource(R.drawable.only_line);
                tv_2.setBackgroundResource(R.drawable.only_line);
                tv_3.setBackgroundResource(R.drawable.only_line);
                tv_4.setBackgroundResource(R.drawable.only_line);
                tv_5.setBackgroundResource(R.drawable.only_line_back);
                tv_6.setBackgroundResource(R.drawable.only_line);
            }
        });

        tv_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed_message.setText(tv_6.getText().toString());

                tv_1.setTextColor(Color.parseColor("#000000"));
                tv_2.setTextColor(Color.parseColor("#000000"));
                tv_3.setTextColor(Color.parseColor("#000000"));
                tv_4.setTextColor(Color.parseColor("#000000"));
                tv_5.setTextColor(Color.parseColor("#000000"));
                tv_6.setTextColor(Color.parseColor("#ffffff"));

                tv_1.setBackgroundResource(R.drawable.only_line);
                tv_2.setBackgroundResource(R.drawable.only_line);
                tv_3.setBackgroundResource(R.drawable.only_line);
                tv_4.setBackgroundResource(R.drawable.only_line);
                tv_5.setBackgroundResource(R.drawable.only_line);
                tv_6.setBackgroundResource(R.drawable.only_line_back);
            }
        });


        tv_send_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


               final String message = ed_message.getText().toString();


                if (!cc.isConnectingToInternet()) {

                }else if (message.equals("")) {

                    cc.showToast("Please enter message");

                    ed_message.requestFocus();

                }else {

                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try  {
                                send(tokken,message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();

                }

            }
        });


        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });


        dialog.show();

    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
       // return mFilteredList.size();
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(context)
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Oops")
                                .setMessage("Error occurred during deleting buudy")
                                .show();
                    } else {
                        String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        FirebaseDatabase.getInstance().getReference().child("friend")
                                .child(StaticConfig.UID).child(idRemoval).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogWaitDeleting.dismiss();

                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Successful")
                                                .setMessage("Buddy deleting successfully")
                                                .show();

                                        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
                                        intentDeleted.putExtra("idFriend", idFriend);
                                        context.sendBroadcast(intentDeleted);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Oops")
                                                .setMessage("Error occurred during deleting buddy")
                                                .show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Oops")
                    .setMessage("Error occurred during deleting buddy")
                    .show();
        }
    }




    public  String send(String to,  String body) {

        try {

            final String apiKey = "AIzaSyDytAz-VgN-I9ViLKGI3mHeRolf5vboNU0";
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + apiKey);
            conn.setDoOutput(true);
            JSONObject message = new JSONObject();
            message.put("to", to);
            message.put("priority", "high");

            JSONObject notification = new JSONObject();
            // notification.put("title", title);
            notification.put("body", body);
            message.put("data", notification);
            OutputStream os = conn.getOutputStream();
            os.write(message.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + message.toString());
            System.out.println("Response Code : " + responseCode);
            System.out.println("Response Code : " + conn.getResponseMessage());

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            dialog.dismiss();
            // print result
            System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }



}

class ItemFriendViewHolder extends RecyclerView.ViewHolder{
    public CircleImageView avata;
    public TextView txtName,txtProfession,txtJoining;
    public LinearLayout ln_click,ln_remove,ln_call,ln_chat;
    private Context context;

    ItemFriendViewHolder(Context context, View itemView) {
        super(itemView);
        avata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        txtProfession = (TextView) itemView.findViewById(R.id.txtProfession);
        txtJoining = (TextView) itemView.findViewById(R.id.txtJoining);

        ln_click = (LinearLayout) itemView.findViewById(R.id.ln_click);
        ln_remove = (LinearLayout) itemView.findViewById(R.id.ln_remove);
        ln_call = (LinearLayout) itemView.findViewById(R.id.ln_call);
        ln_chat = (LinearLayout) itemView.findViewById(R.id.ln_chat);
        this.context = context;
    }
}


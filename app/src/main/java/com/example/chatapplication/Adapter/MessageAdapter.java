package com.example.chatapplication.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.GlobalHelper;
import com.example.chatapplication.models.FmessageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<FmessageModel> fmessageModelList;
    private final int VIEW_TYPE_MY_MESSAGE = 0;
    private final int VIEW_TYPE_FRIEND_MESSAGE = 2;
    private final int VIEW_TYPE_LOADING = 4;

    private boolean isLoading;
    boolean show_loading = true;
    RecyclerView rv;
    private Activity activity;
    private String userId;

    final String TAG = "conv_adapter";
    CollectionReference docRef;
    private DocumentSnapshot lastVisible;
    FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();

    public MessageAdapter(Activity activity, RecyclerView rv, String userId, List<FmessageModel> fmessageModelList, CollectionReference collectionReference) {

        this.activity = activity;
        this.fmessageModelList = fmessageModelList;
        this.docRef = collectionReference;
        this.rv = rv;
        this.userId = userId;
        initRealTimeListener();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_message_my, viewGroup, false);
            return new MyMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_message_friend, viewGroup, false);
            return new FriendMessageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof MyMessageViewHolder) {
            MyMessageViewHolder myMessageViewHolder = (MyMessageViewHolder) holder;

            if (fmessageModelList != null) {

                FmessageModel fmessageModel = fmessageModelList.get(position);
                myMessageViewHolder.myMessageTxt.setText(fmessageModel.content);
                Date date;
                if (fmessageModel.date != null)
                    date = fmessageModel.date;
                else
                    date = fmessageModel.my_date;
                myMessageViewHolder.timeTxt.setText(GlobalHelper.GetTimeString(date));

            }

        } else if (holder instanceof FriendMessageViewHolder) {
            FriendMessageViewHolder friendMessageViewHolder = (FriendMessageViewHolder) holder;

            if (fmessageModelList != null) {

                FmessageModel fmessageModel = fmessageModelList.get(position);
                friendMessageViewHolder.friendMessageTxt.setText(fmessageModel.content);
                Date date;
                if (fmessageModel.date != null)
                    date = fmessageModel.date;
                else
                    date = fmessageModel.my_date;
                friendMessageViewHolder.timeTxt.setText(GlobalHelper.GetTimeString(date));

            }
        }
    }

    @Override
    public int getItemCount() {
        if (fmessageModelList != null)
            return fmessageModelList.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (fmessageModelList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (fmessageModelList.get(position).user_id.equals(userId)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_FRIEND_MESSAGE;
        }

    }

    class FriendMessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout friendMessageLY;
        TextView friendMessageTxt;
        TextView timeTxt;

        public FriendMessageViewHolder(View itemView) {
            super(itemView);

            friendMessageLY = itemView.findViewById(R.id.friendMessageLY);
            friendMessageTxt = itemView.findViewById(R.id.friendMessageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);

        }
    }

    class MyMessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout myMessageLY;
        TextView myMessageTxt;
        TextView timeTxt;
        LinearLayout constraintLayout;

        public MyMessageViewHolder(View itemView) {
            super(itemView);

            myMessageLY = itemView.findViewById(R.id.myMessageLY);
            myMessageTxt = itemView.findViewById(R.id.myMessageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            constraintLayout = itemView.findViewById(R.id.container);

            constraintLayout.setOnClickListener(v -> {

                FmessageModel fmessageModel = fmessageModelList.get(getAdapterPosition());

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Delete Message");
                builder.setMessage("Are you sure to delete Message");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    int position = getAdapterPosition();

                    fireStoreDB.collection(Constants.USER).document(fmessageModel.user_id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error deleting document", e);
                                }
                            });

                    fmessageModelList.remove(position);
                    notifyItemRemoved(position);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                });
                builder.create().show();
            });

        }
    }

    private void initRealTimeListener() {
        LoadData(true);
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void LoadData(final boolean isFirst) {
        Query query;
        query = docRef
                .orderBy("date", Query.Direction.DESCENDING);
        query.addSnapshotListener((value, e) -> {

            if (isLoading) {
                fmessageModelList.remove(0/*fmessageModelList.size() - 1*/);
                notifyItemRemoved(0/*fmessageModelList.size()*/);
            }

            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if ((value != null ? value.size() : 0) == 0)
                show_loading = false;
            List<FmessageModel> messagesList = new ArrayList<>();
            boolean isAdd = false;
            for (DocumentChange doc : value.getDocumentChanges()) {

                switch (doc.getType()) {
                    case ADDED:

                        try {
                            Log.i(TAG, "Log isLoading " + isLoading);

                            lastVisible = value.getDocuments()
                                    .get(value.size() - 1);

                            FmessageModel fmessageModel = doc.getDocument().toObject(FmessageModel.class);
                            messagesList.add(fmessageModel);

                            isAdd = true;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case MODIFIED:
//                                    Log.d(TAG, doc.getDocument().getId() + " => " + doc.getDocument().getData());
                        break;
                    case REMOVED:
//                                    Log.d(TAG, "Removed city: " + doc.getDocument().getData());
                        break;
                }
            }

            if (fmessageModelList != null) {
                if (isLoading) {
                    int pos = fmessageModelList.size() - 1;
                    fmessageModelList.addAll(messagesList);
                    notifyItemRangeInserted(pos, messagesList.size());
                } else {
                    fmessageModelList.addAll(0, messagesList);
                    notifyItemRangeInserted(0, messagesList.size());
                    rv.scrollToPosition(0);
                }
            }

            if (isLoading)
                setLoaded();
        });
    }

    public MessageAdapter getAdapter() {
        return this;
    }

}


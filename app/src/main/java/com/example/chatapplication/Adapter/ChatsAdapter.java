package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.MessageActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.DateHandler;
import com.example.chatapplication.databinding.ItemUserBinding;
import com.example.chatapplication.models.ChatModel;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.UserViewHolder> {

    public Context context;
    public List<ChatModel> list;

    String myUserId;

    public ChatsAdapter(Context context, List<ChatModel> newsList, String myuserId) {
        this.context = context;
        this.list = newsList;
        this.myUserId = myuserId;

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        ChatModel chatModel = list.get(position);

        String friendName, friendAvatar = "";
        if (myUserId.equals(chatModel.sender_id)) {
            friendName = chatModel.friend_name;
            friendAvatar = chatModel.friend_avatar;
        } else {
            friendName = chatModel.sender_name;
            friendAvatar = chatModel.sender_avatar;
        }

        holder.binding.name.setText(friendName);
        String date = DateHandler.GetDateString(chatModel.created_at,"yyyy-MM-dd hh:mm aa");
        holder.binding.messageDatTv.setText(date);

        Glide.with(context).asBitmap().load(friendAvatar).placeholder(R.drawable.profile).into(holder.binding.messageProfileImage);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;
        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatModel chatModel = list.get(getAdapterPosition());

                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(Constants.KEY_CHAT_ID, chatModel.id);
                    context.startActivity(intent);
                }
            });


        }
    }
}

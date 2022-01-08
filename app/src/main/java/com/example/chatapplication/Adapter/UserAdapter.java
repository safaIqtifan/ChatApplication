package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chatapplication.MessageActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.DataCallBack;
import com.example.chatapplication.models.UserModel;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public Context context;
    public List<UserModel> list;

    public UserAdapter(Context context, List<UserModel> newsList) {
        this.context = context;
        this.list = newsList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UserViewHolder viewHolder = new UserViewHolder(inflater.inflate(R.layout.item_user, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        UserModel userModel = list.get(position);

        holder.name.setText(userModel.username);
        Glide.with(context).asBitmap().load(userModel.imageURL).placeholder(R.drawable.profile).into(holder.messageImage);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView messageBody;
        ImageView messageImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            messageBody = itemView.findViewById(R.id.message_body);
            messageImage = itemView.findViewById(R.id.message_profile_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserModel userModel = list.get(getAdapterPosition());

                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(Constants.KEY_USER_MODEL, userModel);
                    context.startActivity(intent);
                }
            });


        }
    }
}
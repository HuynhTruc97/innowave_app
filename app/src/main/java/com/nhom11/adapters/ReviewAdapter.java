package com.nhom11.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom11.models.Review;
import com.nhom11.innowave.R;
import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private Context context;
    private List<Review> reviews;
    private Map<Integer, String> userNames;
    private Map<Integer, Integer> userAvatars; // resource id hoặc url nếu có

    public ReviewAdapter(Context context, List<Review> reviews, Map<Integer, String> userNames, Map<Integer, Integer> userAvatars) {
        this.context = context;
        this.reviews = reviews;
        this.userNames = userNames;
        this.userAvatars = userAvatars;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvName.setText(userNames.getOrDefault(review.user_id, "Người dùng ẩn danh"));
        holder.tvDate.setText(review.created_at != null ? review.created_at.split(" ")[0] : "");
        holder.tvContent.setText(review.comment);
        // Avatar: nếu có userAvatars thì set, không thì để mặc định
        if (userAvatars != null && userAvatars.containsKey(review.user_id)) {
            holder.imgAvatar.setImageResource(userAvatars.get(review.user_id));
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_user);
        }
        // Hiển thị số sao
        holder.numbStar.removeAllViews();
        for (int i = 0; i < review.rating; i++) {
            ImageView star = new ImageView(context);
            star.setImageResource(R.drawable.ic_star);
            star.setColorFilter(context.getResources().getColor(R.color.warning_color));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(18), dp(18));
            if (i > 0) params.setMarginStart(dp(2));
            star.setLayoutParams(params);
            holder.numbStar.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDate, tvContent;
        LinearLayout numbStar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContent = itemView.findViewById(R.id.tvContent);
            numbStar = itemView.findViewById(R.id.numb_star);
        }
    }

    private int dp(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
} 
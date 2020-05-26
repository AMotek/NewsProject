package com.example.newsproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    interface MyNewsListener {

        void onNewsClicked(int position, View view);
    }

    private List<NewsReport> news;
    private MyNewsListener listener;

    public void setListener(MyNewsListener listener) {
        this.listener = listener;
    }

    public NewsAdapter(List<NewsReport> news) {
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_card_view,
                parent, false);
        NewsViewHolder newsViewHolder = new NewsViewHolder(view);
        return newsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        //TODO change holder.newIv to correct image
        NewsReport newsReport = news.get(position);
        setHeadLineImage(holder.newIv, newsReport.getImageURL());
        holder.dateTv.setText(newsReport.getDate());
        if(newsReport.getContent().equals("null"))  holder.contentTv.setText("");
        else holder.contentTv.setText(newsReport.getContent());
        holder.titleTv.setText(newsReport.getTitle());
        setProperIcon(newsReport, holder.siteIcon);
    }

    private void setHeadLineImage(ImageView newsIv, String imageUrl) {

        if(imageUrl.equals("null")) newsIv.setImageResource(R.drawable.breaking_news);
        else Picasso.get().load(imageUrl).resize(500, 500).centerInside().into(newsIv);
    }

    private void setProperIcon(NewsReport newsReport, ImageView iconView) {

        if(newsReport.getSiteName().contains("ynet"))
            iconView.setImageResource(R.drawable.ynet_icon);
        else if(newsReport.getSiteName().contains("israelhayom"))
            iconView.setImageResource(R.drawable.israel_hayom_icon);
        else if(newsReport.getSiteName().contains("0404"))
            iconView.setImageResource(R.drawable.icon0404);
        else if(newsReport.getSiteName().contains("one"))
            iconView.setImageResource(R.drawable.one_icon);
        else if(newsReport.getSiteName().contains("themarker"))
            iconView.setImageResource(R.drawable.themarker_icon);
        else if(newsReport.getSiteName().contains("walla"))
            iconView.setImageResource(R.drawable.walla_icon);
        else if(newsReport.getSiteName().contains("funder"))
            iconView.setImageResource(R.drawable.funder_icon);
        else if(newsReport.getSiteName().contains("calcalist"))
            iconView.setImageResource(R.drawable.calcalist_icon);
        else if(newsReport.getSiteName().contains("sport5"))
            iconView.setImageResource(R.drawable.sport5_icon);
        else if(newsReport.getSiteName().contains("mako"))
            iconView.setImageResource(R.drawable.mako_icon);
        else if(newsReport.getSiteName().contains("geektime"))
            iconView.setImageResource(R.drawable.geek_time_icon);
        else if(newsReport.getSiteName().contains("haaretz"))
            iconView.setImageResource(R.drawable.haaretz_icon);
        else if(newsReport.getSiteName().contains("maariv"))
            iconView.setImageResource(R.drawable.maariv_icon);
        else if(newsReport.getSiteName().contains("globes"))
            iconView.setImageResource(R.drawable.globes_icon);
        else
            iconView.setImageResource(R.drawable.default_icon);
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /*-------------------------------------------Holder class-------------------------------------*/
    public class NewsViewHolder extends RecyclerView.ViewHolder {

        ImageView newIv;
        ImageView siteIcon;
        TextView titleTv;
        TextView contentTv;
        TextView dateTv;

        public NewsViewHolder(@NonNull View itemView) {

            super(itemView);
            this.newIv = itemView.findViewById(R.id.news_iv);
            this.titleTv = itemView.findViewById(R.id.title_tv);
            this.contentTv = itemView.findViewById(R.id.content_tv);
            this.dateTv = itemView.findViewById(R.id.date_tv);
            this.siteIcon = itemView.findViewById(R.id.site_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener != null)
                        listener.onNewsClicked(getAdapterPosition(), view);
                }
            });
        }
    }
}

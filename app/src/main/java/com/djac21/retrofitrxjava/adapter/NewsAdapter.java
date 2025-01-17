package com.djac21.retrofitrxjava.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.djac21.retrofitrxjava.R;
import com.djac21.retrofitrxjava.customTabs.CustomTabActivityHelper;
import com.djac21.retrofitrxjava.activities.WebViewActivity;
import com.djac21.retrofitrxjava.models.NewsModel;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsModel.Articles> news;
    private Context context;

    public NewsAdapter(List<NewsModel.Articles> news, Context context) {
        this.news = news;
        this.context = context;
    }

    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = 0;
        try {
            time = simpleDateFormat.parse(news.get(position).getPublishedAt()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());

        holder.title.setText(nullCheck(news.get(position).getTitle()));
        holder.author.setText(nullCheck(news.get(position).getAuthor()));
        holder.description.setText(nullCheck(news.get(position).getDescription()));
        holder.date.setText(nullCheck(prettyTime.format(new Date(time))));
        Glide.with(context)
                .load(news.get(position).getUrlToImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_photo)
                        .error(R.drawable.ic_photo))
                .into(holder.image);
    }

    private String nullCheck(String inputString) {
        if (inputString == null)
            return "N/A";

        return inputString;
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, author, description, date;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            author = view.findViewById(R.id.author);
            description = view.findViewById(R.id.description);
            date = view.findViewById(R.id.date);
            image = view.findViewById(R.id.image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            String url = news.get(position).getUrl();

            if (validateUrl(url)) {
                Uri uri = Uri.parse(url);
                if (uri != null)
                    openCustomChromeTab(uri);
            } else {
                Toast.makeText(context, "Error with link", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean validateUrl(String url) {
            return url != null && url.length() > 0 && (url.startsWith("http://") || url.startsWith("https://"));
        }

        private void openCustomChromeTab(Uri uri) {
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = intentBuilder.build();

            intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            CustomTabActivityHelper.openCustomTab(context, customTabsIntent, uri, new CustomTabActivityHelper.CustomTabFallback() {
                @Override
                public void openUri(Context activity, Uri uri) {
                    openWebView(uri);
                }
            });
        }

        private void openWebView(Uri uri) {
            Intent webViewIntent = new Intent(context, WebViewActivity.class);
            webViewIntent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
            webViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webViewIntent);
        }
    }
}
package com.nerdability.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nerdability.android.R;
import com.nerdability.android.adapter.ArticleListAdapter;
import com.nerdability.android.db.DbAdapter;
import com.nerdability.android.rss.domain.Article;
import com.nerdability.android.util.DateUtils;

public class ArticleDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    Article displayedArticle;
    DbAdapter db;

    public ArticleDetailFragment() {
    	setHasOptionsMenu(true);	//this enables us to set actionbar from fragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DbAdapter(getActivity());
        if (getArguments().containsKey(Article.KEY)) {
            displayedArticle = (Article) getArguments().getSerializable(Article.KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        if (displayedArticle != null) {
        	String title = displayedArticle.getTitle();
        	String pubDate = displayedArticle.getPubDate();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss Z", Locale.ENGLISH);
            try {
				Date pDate =  df.parse(pubDate);
				pubDate = "This post was published " + DateUtils.getDateDifference(pDate) + " by " + displayedArticle.getAuthor();
			} catch (ParseException e) {
				Log.e("DATE PARSING", "Error parsing date..");
				pubDate = "published by " + displayedArticle.getAuthor();
			} 
            
        	String content = displayedArticle.getEncodedContent();
        	((TextView) rootView.findViewById(R.id.article_title)).setText(title);
        	((TextView) rootView.findViewById(R.id.article_author)).setText(pubDate);
            ((TextView) rootView.findViewById(R.id.article_detail)).setText(Html.fromHtml(content));
        }
        return rootView;
    }
    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailmenu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("item ID : ", "onOptionsItemSelected Item ID" + id);
        if (id == R.id.actionbar_saveoffline) {
        	Toast.makeText(getActivity().getApplicationContext(), "This article has been saved of offline reading.", Toast.LENGTH_LONG).show();
        	return true;
        } else if (id == R.id.actionbar_markunread) {
            db.openToWrite();
            db.markAsUnread(displayedArticle.getGuid());
            db.close();
            displayedArticle.setRead(false);
            ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.article_list)).getListAdapter();
            adapter.notifyDataSetChanged();
        	return true;
        } else {
        	return super.onOptionsItemSelected(item);
        }
    }
}
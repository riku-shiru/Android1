package org.rikushiru.play.rikushirutter;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterOAuthActivity extends ActionBarActivity {

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_oauth);

        mCallbackURL = getString(R.string.twitter_callback_url);
        mTwitter = TwitterUtils.getTwitterInstance(this);

        findViewById(R.id.action_start_oauth).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startAuthorize();
            }
        });
    }

    private void startAuthorize(){
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try{
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
        protected void onPostExecute(String url){
                if(url != null){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }else{
                    // failed
                }
            }
        };
        task.execute();
    }
    @Override
    public void onNewIntent(Intent intent){
        if(intent == null || intent.getData().toString().startsWith(mCallbackURL)){
            return;
        }
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try{
                    return mTwitter.getOAuthAccessToken(mRequestToken,params[0]);
                }catch(TwitterException e){
                    e.printStackTrace();
                }
                return null;
            }
            @Override
        protected void onPostExecute(AccessToken accessToken){
                if(accessToken != null){
                    //success
                    showToast("Success!!");
                    successOAuth(accessToken);
                } else {
                    //failed
                    showToast("Failed");
                }
            }
        };
        task.execute(verifier);
    }

    private void successOAuth(AccessToken accessToken){
        TwitterUtils.storeAccessToken(this, accessToken);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter_oauth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

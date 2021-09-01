package com.yourcompany.inapprating;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    int SESSIONS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp2 = getSharedPreferences("Alert",MODE_PRIVATE);
        TextView t = findViewById(R.id.txt);
        t.setText("Session "+sp2.getInt("value",0));



        RatingDialog();

    }

    private void RatingDialog() {
        //check if value of shared pref
        SharedPreferences rateStatus = getSharedPreferences("Status",MODE_PRIVATE);
        int status = rateStatus.getInt("Showed",1);

        //status == 1 means user have never respond to rating dialog in this case show the rating dialog
        if(status == 1){
            try {

                //but before showing the dialog check the session number, no one like it to see same rating
                //dialog again and again therefore I have set the session number limit to 1 you can set it according to your choice
                SharedPreferences sp = getSharedPreferences("Alert",MODE_PRIVATE);
                int value = sp.getInt("value",0);
                if(value == SESSIONS){
                    showRatingPopUp();

                    SharedPreferences.Editor spEditor = sp.edit();
                    spEditor.putInt("value",0);
                    spEditor.apply();
                } else {
                    SharedPreferences.Editor spe = sp.edit();
                    spe.putInt("value",value+1);
                    spe.apply();
                }
            } catch (Exception ignored){

            }
        }
    }


    private void showRatingPopUp(){
        //we will crate and show the main alert here

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.rate_us,null);

        final RatingBar rb = view.findViewById(R.id.ratingBar);
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //if user click on submit button then change status to 0
                        SharedPreferences rateStatus = MainActivity.this.getSharedPreferences("Status", MODE_PRIVATE);
                        SharedPreferences.Editor ed = rateStatus.edit();
                        ed.putInt("Showed", 0);
                        ed.apply();


                        // check the value of user rating and take the related action
                        String rating = String.valueOf(rb.getRating());
                        if (rating.equals("4.0") || rating.equals("5.0")) {
                            MainActivity.this.PlayStoreIntent();
                        } else {

                            MainActivity.this.feedbackForm();
                        }
                    }
                })
                .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();



    }

    private void feedbackForm() {
        //we will design and show feedback form here
        View v = getLayoutInflater().inflate(R.layout.feedback_form, null);
        final EditText txt = v.findViewById(R.id.feedback_editText);
        final AlertDialog d = new AlertDialog.Builder(this)
                .setView(v)
                .show();
        Button submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txt.getText().toString().isEmpty()) {
                    txt.setError("Field can't be null");
                    txt.requestFocus();
                    return;
                }




                //send user's response to email
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MainActivity.this.getString(R.string.CONTACT_EMAIL)});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, txt.getText().toString());
                try {
                    if (intent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        MainActivity.this.startActivity(intent);
                        d.dismiss();
                    }
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void PlayStoreIntent() {
        Intent view = new Intent();
        view.setAction(Intent.ACTION_VIEW);
        view.setData(Uri.parse(getResources().getString(R.string.PlayStoreLink)));
        startActivity(view);
    }

}
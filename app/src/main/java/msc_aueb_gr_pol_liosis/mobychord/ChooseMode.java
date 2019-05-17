package msc_aueb_gr_pol_liosis.mobychord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class ChooseMode extends Activity {

    //**********GUI variables**********
    private Button makeRequestButton;
    private Button makeNodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        makeRequestButton = (Button) this.findViewById(R.id.makeRequestButton);
        makeNodeButton = (Button) this.findViewById(R.id.makeNodeButton);

        makeRequestButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View v)
            {
                makeNewRequest();
            }
        });

        makeNodeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View v)
            {
                newChordNodeRequest();
            }
        });

        createAppDirectories();
    }


    private void makeNewRequest()
    {
        Intent intent = new Intent(this, RequestClient.class);
        startActivity(intent);
    }

    private void newChordNodeRequest()
    {
        Intent intent = new Intent(this, NewNodeInfoActivity.class);
        startActivity(intent);
    }

    private void createAppDirectories()
    {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node");
        success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

//        folder = new File(Environment.getExternalStorageDirectory() +
//                File.separator + "mobyChord/Client");
//        success = true;
//        if (!folder.exists()) {
//            success = folder.mkdirs();
//        }

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node/Net Architecture");
        success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node/Keys");
        success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

    }

}

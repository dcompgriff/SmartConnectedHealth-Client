package layout;
/**
 * Author: Daniel Griffin
 * */
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sch.trustworthysystems.smartconnectedhealth_client.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DataInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataInputFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int CAMERA_INTENT_CODE = 0;
    private static final int GALLERY_INTENT_CODE = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Private data references
    private HashMap<String, Double> mealMap = new HashMap<String, Double>();
    private String previousImagePath = "";

    // UI references
    private Button mIm2CalButton;
    private Button mIm2CalGalleryButton;
    private Button mAddMealButton;
    private DatePicker mMealDatePicker;
    private TimePicker mMealTimePicker;
    private ImageView mIm2CalImageView;
    // Meal Input Edit Text
    private EditText mMealCaloriesEditText;
    private EditText mMealCarbsEditText;
    private EditText mMealCholestEditText;
    private EditText mMealFatEditText;
    private EditText mMealFiberEditText;
    private EditText mMealProteinEditText;
    private EditText mMealSodiumEditText;
    private EditText mMealSugarsEditText;


    public DataInputFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataInputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataInputFragment newInstance(String param1, String param2) {
        DataInputFragment fragment = new DataInputFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Main functional code goes here because at this point the layout has been inflated and
     * we can get references to views defined in xml.
     * */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get references to the UI elements.
        mIm2CalButton = (Button) getActivity().findViewById(R.id.im2cal_button);
        mIm2CalGalleryButton = (Button) getActivity().findViewById(R.id.im2cal_gallery_button);
        mAddMealButton = (Button) getActivity().findViewById(R.id.add_meal_button);
        mMealDatePicker = (DatePicker) getActivity().findViewById(R.id.meal_date_picker);
        mMealTimePicker = (TimePicker) getActivity().findViewById(R.id.meal_time_picker);
        mIm2CalImageView = (ImageView) getActivity().findViewById(R.id.im2cal_image_view);
        // Get references to the meal UI edit text inputs.
        mMealCaloriesEditText = (EditText) getActivity().findViewById(R.id.meal_calories_edit_text);
        mMealCarbsEditText = (EditText) getActivity().findViewById(R.id.meal_carbs_edit_text);
        mMealCholestEditText = (EditText) getActivity().findViewById(R.id.meal_cholest_edit_text);
        mMealFatEditText = (EditText) getActivity().findViewById(R.id.meal_fat_edit_text);
        mMealFiberEditText = (EditText) getActivity().findViewById(R.id.meal_fiber_edit_text);
        mMealProteinEditText = (EditText) getActivity().findViewById(R.id.meal_protein_edit_text);
        mMealSodiumEditText = (EditText) getActivity().findViewById(R.id.meal_sodium_edit_text);
        mMealSugarsEditText = (EditText) getActivity().findViewById(R.id.meal_sugars_edit_text);

        // Attach listeners to the Im2Cal button and Add Meal button.
        mIm2CalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set file path parameters in camera intent.
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                // Create a unique file name, and save the image path to be used in the onActivityResult() function to get
                // the saved image.
                Date mDate = new Date();
                String currentDate = mDate.toString();
                currentDate = currentDate.replaceAll(" ", "_");
                File output = new File(dir, currentDate + ".jpg");
                previousImagePath = output.getAbsolutePath();

                // Make an intent to start the camera to take an image of a meal.
                Intent cameraIntent = new Intent();
                cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
                startActivityForResult(cameraIntent, CAMERA_INTENT_CODE);

                //Toast.makeText(getContext(), "Work in progress...", Toast.LENGTH_SHORT).show();
            }
        });
        mIm2CalGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image."), GALLERY_INTENT_CODE);
            }
        });
        mAddMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mealMap.put("Calories", Double.parseDouble(mMealCaloriesEditText.getText().toString()));
                mealMap.put("Carbs", Double.parseDouble(mMealCarbsEditText.getText().toString()));
                mealMap.put("Cholest", Double.parseDouble(mMealCholestEditText.getText().toString()));
                mealMap.put("Fat", Double.parseDouble(mMealFatEditText.getText().toString()));
                mealMap.put("Fiber", Double.parseDouble(mMealFiberEditText.getText().toString()));
                mealMap.put("Protein", Double.parseDouble(mMealProteinEditText.getText().toString()));
                mealMap.put("Sodium", Double.parseDouble(mMealSodiumEditText.getText().toString()));
                mealMap.put("Sugars", Double.parseDouble(mMealSugarsEditText.getText().toString()));

                // TODO: Add to datetime to parse to custom date and time format.
                int month = mMealDatePicker.getMonth();
                int day = mMealDatePicker.getDayOfMonth();
                int year = mMealDatePicker.getYear();
                int hour = mMealTimePicker.getCurrentHour();
                int minute = mMealTimePicker.getCurrentMinute();

                // Make a toast to show the contents of the meal map.
                // Toast.makeText(getContext(), mealMap.toString(), Toast.LENGTH_SHORT).show();
                // Pass the meal map to the function to send it to the server.
                sendMealDataToServer(mealMap);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.data_input_fragment, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This function is used to send new meal information to the server.
     * */
    private void sendMealDataToServer(HashMap<String, Double> mealMap){

        /**
         * 1) Make json output string.
         * 2) Create async task with the json string.
         * 3) Run async task.
         * */

        // WRITE THE OUTPUT TO THE STREAM.
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String mealMapString = gson.toJson(mealMap);
        // TODO: Make and call new async task to do the http post.
        //AsyncTask<String, Integer, String> postTask = new PostMealDataTask();
        //postTask.execute(mealMapString);
        Toast.makeText(getContext(), "JSON: " + mealMapString, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is called when an activity started through an intent using startActivityForResult() returns.
     * It is currently used to retrieve the result of an image capture.
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_INTENT_CODE){
            if (resultCode == getActivity().RESULT_OK){
                // Get the image url returned from the intent.
                Uri imageUri = Uri.fromFile(new File(previousImagePath));

                // Make a bitmap and options object to load the saved image into memory.
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 4;

                String path = imageUri.getPath();
                String encodedPath = imageUri.getEncodedPath();
                String pathToString = imageUri.toString();

                mIm2CalImageView.setImageURI(null);
                mIm2CalImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mIm2CalImageView.setImageBitmap(BitmapFactory.decodeFile(imageUri.getPath(), options));
                //mIm2CalImageView.setImageURI(null);
                //mIm2CalImageView.setImageURI(imageUri);
                mIm2CalImageView.postInvalidate();
            }
        }else if (requestCode == GALLERY_INTENT_CODE){
            if (resultCode == getActivity().RESULT_OK){
                // Get the absolute file path from the media store uri.
                Cursor c = getActivity().getContentResolver().query(data.getData(),null,null,null,null);
                c.moveToNext();
                String imagePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                c.close();

                // Load the bitmap and set it in the image view.
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 4;
                mIm2CalImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath , options));
            }
        }



    }

    private class PostMealDataTask extends AsyncTask<String, Integer, String>{
        /**
         * This method sets up the work to to in the background.
         * */
        @Override
        protected String doInBackground(String... strings) {
            // Set up connection to the server to post data.
            URL url = null;
            try {
                url = new URL("http://www.android.com/");
            } catch (MalformedURLException e){
                Toast.makeText(getContext(), "Malformed URL exception for submitting meal.", Toast.LENGTH_SHORT).show();
            }
            // Create the url object to call the http service.
            HttpURLConnection urlConnection = null;
            try {
                // Open the url connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                // Indicate that the url stream is an output, and that the output length is unknown.
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                // WRITE THE OUTPUT TO THE STREAM.
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                String mealMapString = gson.toJson(mealMap);
                out.close();

                // Create input stream to read the response.
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                // READ THE INPUT FROM THE STREAM.
                //Type token = new TypeToken<Map<String, String>>(){}.getType();
                //Map<String, String> map =  gson.fromJson("{'key1':'123','key2':'456'}", type);

//            String decodedString;
//            while ((decodedString = in.readLine()) != null) {
//                System.out.println(decodedString);
//            }
//            in.close();
            }catch (IOException e){
                Toast.makeText(getContext(), "Url IO exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return "Result response goes here.";
        }

    }

}

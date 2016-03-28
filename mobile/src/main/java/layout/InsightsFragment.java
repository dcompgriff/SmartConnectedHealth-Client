package layout;
/**
 * Author: Daniel Griffin
 * */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.sch.trustworthysystems.smartconnectedhealth_client.MainActivity;
import com.sch.trustworthysystems.smartconnectedhealth_client.MyYAxisValueFormatter;
import com.sch.trustworthysystems.smartconnectedhealth_client.R;
import com.sch.trustworthysystems.smartconnectedhealth_client.view.MyMarkerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import com.google.gson.JsonParser;

// Imports for the spider plot.
// Imports for the bar chart.
import android.graphics.PointF;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;


class RefreshRequest {
    String timestamp;
}


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link InsightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InsightsFragment extends Fragment {

    // Index for all series
    private int normalSeriesIndex;
    private int highSeriesIndex;
    private int dangerousSeriesIndex;
    private float lineWidth = 32.f;
    private String mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_NORMAL;
    private String UPDATE_WATCH_PEAK_GLUCOSE = "/update_peak_glucose";
    private GoogleApiClient mApiClient;

    // Labels for synthetic spider chart plot.
    private String[] HealthAttrs = new String[] {
            "Calories", "Carbs", "Cholesterol", "Fat", "Fiber", "Protein", "Sodium", "Sugars"
    };

    private String[] InspirationMessages = new String[] {
            "Keep at it!",
            "You have hit your health target every day this week, congratulations!",
            "One foot in the grave!",
            "There's no helping you!",
            "It is certain.",
            "It is decidedly so.",
            "Without a doubt.",
            "Yes, definitely.",
            "You may rely on it.",
            "As I see it, yes.",
            "Most likely.",
            "Outlook good."
    };
    private class PastData {
        public ArrayList<Float> BarValues;
        public ArrayList<String> MealLabels;

        public PastData() {
            BarValues = new ArrayList<Float>();
            MealLabels = new ArrayList<String>();
        }
    }

    private class MealData {
        public HashMap<String, Float> AvgMap, CurMap;

        public MealData() {
            AvgMap = new HashMap<String, Float>();
            CurMap = new HashMap<String, Float>();
        }
    }

    private class GlucoseLabelPercentages{
        public float Normal, High, Dangerous;

        public GlucoseLabelPercentages(float norm, float high, float danger){
            this.Normal = norm;
            this.High = high;
            this.Dangerous = danger;
        }
    }

    private class HealthData {
        GlucoseLabelPercentages LabelObject;
        MealData MealObject;
        PastData PastObject;

        public HealthData(GlucoseLabelPercentages labels, MealData meals, PastData past) {
            LabelObject = labels;
            MealObject = meals;
            PastObject = past;
        }
    }

    // UI references.
    private DecoView arcView;
    private RadarChart mChart;
    private BarChart mBarChart;
    private ImageButton refreshButton;

    public InsightsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InsightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*public static InsightsFragment newInstance() {
        InsightsFragment fragment = new InsightsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }*/

    /**
     * Main functional code goes here because at this point the layout has been inflated and
     * we can get references to views defined in xml.
     * */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create the bar graph view, and add it to this fragment's layout.
        createGlucosePeakBarChart();
        // Create the spider view, and add it to this fragment's layout.
        createSpiderView();
        // Create the deco view, and add it to this fragment's layout.
        createDecoView();
        // Create refresh button actions
        setupRefreshComponents();
        // Create the google api client.
        initGoogleApiClient();
    }

    private SeriesItem createStandardSeriesItem(int color, String confidenceType, float insetMultiple){

        //Create data series track
        SeriesItem seriesItem1 = new SeriesItem.Builder(ContextCompat.getColor(getContext(), color))
                .setRange(0.f, 1.f, 0.f)
                .setLineWidth(lineWidth)
                .setInitialVisibility(false)
                //.setSeriesLabel(new SeriesLabel.Builder(confidenceType + " %.0f%%").build())
                .setInterpolator(new OvershootInterpolator())
                .setSpinClockwise(true)
                .setSpinDuration(3000)
                .setInset(new PointF(lineWidth * insetMultiple, lineWidth * insetMultiple))
                .build();

        return seriesItem1;
    }

    private void createDecoView(){
        arcView = (DecoView)getActivity().findViewById(R.id.dynamicArcView);

        // Create background track
        arcView.addSeries(new SeriesItem.Builder(ContextCompat.getColor(getContext(), R.color.backgroundTrack))
                .setRange(0.f, 1.f, 1.f)
                .setLineWidth(lineWidth * 3)
                .setInitialVisibility(true)
                .setInset(new PointF(lineWidth * 2, lineWidth * 2))
                .build());

        SeriesItem seriesItem3 = createStandardSeriesItem(R.color.colorDangerous, "Dangerous", 1.f);
        dangerousSeriesIndex = arcView.addSeries(seriesItem3);

        SeriesItem seriesItem2 = createStandardSeriesItem(R.color.colorHigh, "High", 2.f);
        highSeriesIndex = arcView.addSeries(seriesItem2);

        SeriesItem seriesItem1 = createStandardSeriesItem(R.color.colorNormal, "Normal", 3.f);
        normalSeriesIndex = arcView.addSeries(seriesItem1);
    }

    /**
     * This function creates the bar graph view, and initializes it with data.
     * */
    private void createGlucosePeakBarChart(){
        //Retrieve a reference to the bar chart.
        mBarChart = (BarChart) getActivity().findViewById(R.id.glucose_peak_bar_chart);

        //Configure properties of the bar chart.
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDescription("");
        mBarChart.setDescriptionPosition(500, 25);
        mBarChart.setPinchZoom(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setDrawGridBackground(false);

        XAxis xLabels = mBarChart.getXAxis();
        xLabels.setPosition(XAxisPosition.BOTTOM);

        mBarChart.getAxisLeft().setEnabled(false);
        mBarChart.getAxisRight().setEnabled(false);

        // Set the properties of the legend.
        Legend l = mBarChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        l.setForm(LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
    }

    /**
     * This function creates the spider view, and initializes it with data.
     * */
    private void createSpiderView(){
        //Retrieve a reference to the radar chart.
        mChart = (RadarChart) getActivity().findViewById(R.id.meal_radar_chart);
        //tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        //MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        // set the marker to the chart
        //mChart.setMarkerView(mv);
        mChart.setDrawMarkerViews(false);
        // Setup chart visuals
        mChart.invalidate();
        mChart.setDescription("");
        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);
        //setSpiderData();
        mChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        //xAxis.setTypeface(tf);
        xAxis.setTextSize(9f);
        YAxis yAxis = mChart.getYAxis();
        //yAxis.setTypeface(tf);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinValue(0f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        //l.setTypeface(tf);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
    }

    /**
     * This function generates synthetic data, and adds it to the bar chart plot.
     *
     * TODO:
     * 1) Retrieve the date for the past few days, up to the beginning of the week.
     * 2) Retreieve the glucose peaks for the past week.
     * 3) Display the glucose values with colors based on the levels.
     *
     * */
    public void setBarChartData(PastData past) {

        // Constants
        int empty = 0, normal = 1, high = 2, dangerous = 3;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < past.BarValues.size(); i++) {
            yVals1.add(new BarEntry(new float[] {
                    empty,
                    (past.BarValues.get(i) == normal) ? normal : empty,
                    (past.BarValues.get(i) == high) ? high : empty,
                    (past.BarValues.get(i) == dangerous) ? dangerous : empty
            }, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Past Peak Glucose Levels");
        set1.setColors(new int[]{
                ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorNormal),
                ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorHigh),
                ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorDangerous)
        });
        set1.setStackLabels(new String[]{"NA", "Normal", "High", "Dangerous"});
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(past.MealLabels, dataSets);

        mBarChart.setData(data);
        mBarChart.invalidate();
    }

    /**
     * This function generates synthetic data, and adds it to the spider plot.
     * */
    public void setSpiderData(MealData meal) {

        ArrayList<Entry> avgYVals = new ArrayList<Entry>();
        ArrayList<Entry> curYVals = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < HealthAttrs.length; i++) {
            avgYVals.add(new Entry(meal.AvgMap.get(HealthAttrs[i]), i));
            curYVals.add(new Entry(meal.CurMap.get(HealthAttrs[i]), i));
        }

        int avgColor = ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorPrimary);
        RadarDataSet set1 = new RadarDataSet(avgYVals, "Average Meal");
        set1.setColor(avgColor);
        set1.setFillColor(avgColor);
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);

        int curColor = ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorAccent);
        RadarDataSet set2 = new RadarDataSet(curYVals, "Current Meal");
        set2.setColor(curColor);//ColorTemplate.VORDIPLOM_COLORS[4]);
        set2.setFillColor(curColor);//ColorTemplate.VORDIPLOM_COLORS[4]);
        set2.setDrawFilled(true);
        set2.setLineWidth(2f);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(HealthAttrs, sets);
        //data.setValueTypeface(tf);
        data.setValueTextSize(8f);
        data.setDrawValues(false);

        mChart.setData(data);

        mChart.invalidate();
    }

    private void setupRefreshComponents() {
        refreshButton = (ImageButton) getActivity().findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshGlucosePeak();
            }
        });

        refreshGlucosePeak();
    }

    private void refreshGlucosePeak() {
        // Get current time as datestring
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        String dateString = df.format(new Date());

        // Build Json response
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.timestamp = "2015-08-06 18:05:54";//dateString;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String refreshMapString = gson.toJson(refreshRequest);

        // Call AsynTask
        RefreshPeakTask refreshPeakTask = new RefreshPeakTask();
        refreshPeakTask.execute(refreshMapString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.insights_fragment, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    /**
     * Implement the google api client for sending messages to the wearable app.
     * */
    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi( Wearable.API )
                .build();
        mApiClient.connect();
    }

    /**
     * Make a thread, and send a message to the android wearable to update it.
     * Note: This method sends a message to every connected node.
     * */
    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                // Exit if no connected nodes.
                if (nodes == null){
                    return;
                }
                // Send the message to each node.
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    private class RefreshPeakTask extends AsyncTask<String, HealthData, HealthData>{
        /**
         * This method sets up the work to to in the background.
         * */
        @Override
        protected HealthData doInBackground(String... strings) {
            // Set up connection to the server to post data.
            URL url = null;
            try {
                url = new URL("http://104.236.167.62:5000/get/glucose/");
            } catch (MalformedURLException e){
                Toast.makeText(getContext(), "Malformed URL exception for submitting meal.", Toast.LENGTH_SHORT).show();
                return null;
            }
            // Create the url object to call the http service.
            HttpURLConnection urlConnection = null;
            try {
                // Open the url connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                // Indicate that the url stream is an output, and that the output length is unknown.
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

                // WRITE THE OUTPUT TO THE STREAM.
                System.out.println(strings[0]);
                out.write(strings[0]);
                out.flush();

                // Create input stream to read the response.
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String decodedString;
                String jsonString = "";
                while ((decodedString = in.readLine()) != null) {
                    System.out.println(decodedString);
                    jsonString += decodedString;
                }

                JsonObject elements = new JsonParser().parse(jsonString).getAsJsonObject();

                float   Normal = elements.getAsJsonPrimitive("normal").getAsFloat(),
                        High = elements.getAsJsonPrimitive("high").getAsFloat(),
                        Dangerous = elements.getAsJsonPrimitive("danger").getAsFloat();

                GlucoseLabelPercentages labels = new GlucoseLabelPercentages(Normal, High, Dangerous);

                // Set the peak glucose level based on the returned scores, and send an intent
                // to update the watch face service.
                //mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_NORMAL;
                /**if (High > Normal && High > Dangerous){
                    mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_HIGH;
                }else if(Dangerous > High && Dangerous > Normal){
                    mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_DANGEROUS;
                }*/
                if(mCurrentPeakGlucose.equals(MainActivity.GLUCOSE_PEAK_LEVEL_NORMAL)){
                    mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_HIGH;
                }else if(mCurrentPeakGlucose.equals(MainActivity.GLUCOSE_PEAK_LEVEL_HIGH)){
                    mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_DANGEROUS;
                }else if(mCurrentPeakGlucose.equals(MainActivity.GLUCOSE_PEAK_LEVEL_DANGEROUS)){
                    mCurrentPeakGlucose = MainActivity.GLUCOSE_PEAK_LEVEL_NORMAL;
                }
//                Intent peakGlucoseChangedIntent = new Intent();
//                peakGlucoseChangedIntent.setAction(MainActivity.ACTION_GLUCOSE_PEAK_CHANGED);
//                peakGlucoseChangedIntent.putExtra(MainActivity.GLUCOSE_PEAK_LEVEL_INTENT_KEY, mCurrentPeakGlucose);
//                getActivity().sendBroadcast(peakGlucoseChangedIntent);
                // Send the android wearable the peak glucose updated message.
                sendMessage(UPDATE_WATCH_PEAK_GLUCOSE, mCurrentPeakGlucose);

                MealData meals = new MealData();
                for (String healthAttr : HealthAttrs) {
                    meals.AvgMap.put(healthAttr, elements.getAsJsonPrimitive("Avg" + healthAttr).getAsFloat());
                    meals.CurMap.put(healthAttr, elements.getAsJsonPrimitive(healthAttr).getAsFloat());
                }

                String label = "Label", meal = "Meal";
                PastData past = new PastData();
                for (int i = 0; i < 4; i++) {
                    String thisLabel = label + i, thisMeal = meal + i;

                    past.BarValues.add(elements.getAsJsonPrimitive(thisLabel).getAsFloat());
                    past.MealLabels.add(elements.getAsJsonPrimitive(thisMeal).getAsString());
                }

                return new HealthData(labels, meals, past);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        protected void onPostExecute(HealthData health) {
            if (health != null) {
                Toast.makeText(getContext(), "Successfully refreshed glucose peak!", Toast.LENGTH_LONG).show();

                arcView.addEvent(new DecoEvent.Builder(health.LabelObject.Normal).setIndex(normalSeriesIndex).build());
                arcView.addEvent(new DecoEvent.Builder(health.LabelObject.High).setIndex(highSeriesIndex).build());
                arcView.addEvent(new DecoEvent.Builder(health.LabelObject.Dangerous).setIndex(dangerousSeriesIndex).build());

                setSpiderData(health.MealObject);
                setBarChartData(health.PastObject);

                TextView inspiration = (TextView) getActivity().findViewById(R.id.inspiration_text);
                int idx = new Random().nextInt(InspirationMessages.length);
                inspiration.setText(InspirationMessages[idx]);

            } else {
                Toast.makeText(getContext(), "Error refreshing peak.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

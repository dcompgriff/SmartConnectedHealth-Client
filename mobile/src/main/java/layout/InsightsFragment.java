package layout;
/**
 * Author: Daniel Griffin
 * */
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;
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
import java.util.Locale;

import com.google.gson.JsonParser;

// Imports for the spider plot.
// Imports for the bar chart.
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONObject;


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

    // Labels for synthetic spider chart plot.
    private String[] mParties = new String[]{
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I"
    };

    //private String[] class_status = new String[] {"Normal", "High", "Dangerous"};

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

        /*arcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(1000)
                .setDuration(2000)
                .build());
        arcView.addEvent(new DecoEvent.Builder(.75f)
                .setIndex(normalSeriesIndex)
                .setDelay(4000).build());
        arcView.addEvent(new DecoEvent.Builder(.25f)
                .setIndex(highSeriesIndex)
                .setDelay(4000).build());
        arcView.addEvent(new DecoEvent.Builder(.5f)
                .setIndex(dangerousSeriesIndex)
                .setDelay(4000).build());
        */
        //arcView.addEvent(new DecoEvent.Builder(25).setIndex(series1Index).setDelay(4000).build());
        //arcView.addEvent(new DecoEvent.Builder(100).setIndex(series1Index).setDelay(8000).build());
        //arcView.addEvent(new DecoEvent.Builder(10).setIndex(series1Index).setDelay(12000).build());
    }

    /**
     * This function creates the bar graph view, and initializes it with data.
     * */
    private void createGlucosePeakBarChart(){
        //Retrieve a reference to the bar chart.
        mBarChart = (BarChart) getActivity().findViewById(R.id.glucose_peak_bar_chart);

        //Configure properties of the bar chart.
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(false);
        mBarChart.setDescription("Max Blood Glucose By Day");
        mBarChart.setDrawValueAboveBar(false);
        mBarChart.setDescriptionPosition(500, 25);
        mBarChart.setMaxVisibleValueCount(7);
        mBarChart.setPinchZoom(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setDrawGridBackground(false);

        // Set the properties of the x-axis.
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(1);

        // Set the properties of the y-axis.
        YAxisValueFormatter custom = new MyYAxisValueFormatter();
        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(false);
        // Set the properties of the right y-axis
        YAxis rightAxis = mBarChart.getAxisRight();
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        rightAxis.setDrawGridLines(false);



        // Set the properties of the legend.
        Legend l = mBarChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        l.setForm(LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        // Set the bars to animate to their proper value in 3 seconds.
        mBarChart.animateY(1500);

        // 7 values, with one of three classes
        setBarChartData(7, 3);

    }


    /**
     * This function creates the spider view, and initializes it with data.
     * */
    private void createSpiderView(){
        //Retrieve a reference to the radar chart.
        mChart = (RadarChart) getActivity().findViewById(R.id.meal_radar_chart);
        //tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        // set the marker to the chart
        mChart.setMarkerView(mv);

        // Setup chart visuals
        mChart.invalidate();
        mChart.setDescription("Post Meals");
        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);
        setSpiderData();
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
    public void setBarChartData(int count, float range) {
            // Create an array of month labels.
            String[] mMonths = new String[]{"M", "T", "W", "Th", "F", "Sa", "Su", "M", "T", "W", "Th", "F", "Sa", "Su" };
            // Create an x axis array of months.
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                xVals.add(mMonths[i % 12]);
            }
            // Create a y axis array of y values. (Currently random values).
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            for (int i = 0; i < count / 1.5; i++) {
                yVals1.add(new BarEntry(1, i));
            }

            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
            for (int i = (int) (count / 1.5) + 1; i < count; i++) {
                yVals2.add(new BarEntry(2, i));
            }

            // Create an object for holding metadata related to the y-values of the bars.
            BarDataSet set1 = new BarDataSet(yVals1, "Normal");
            set1.setBarSpacePercent(35f);
            // Set the color of the bars in the bar chart.
            set1.setColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorNormal));

            BarDataSet set2 = new BarDataSet(yVals2, "High");
            set2.setBarSpacePercent(35f);
            // Set the color of the bars in the bar chart.
            set2.setColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorHigh));

            //Add the bars to the set of y-values to be plotted.
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);

            //Add an x axis value for all of the bars to be plotted
            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            //data.setValueTypeface(mTf);

            mBarChart.setData(data);
    }

    /**
     * This function generates synthetic data, and adds it to the spider plot.
     * */
    public void setSpiderData() {

        float mult = 150;
        int cnt = 9;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < cnt; i++) {
            yVals1.add(new Entry((float) (Math.random() * mult) + mult / 2, i));
        }

        for (int i = 0; i < cnt; i++) {
            yVals2.add(new Entry((float) (Math.random() * mult) + mult / 2, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < cnt; i++)
            xVals.add(mParties[i % mParties.length]);

        RadarDataSet set1 = new RadarDataSet(yVals1, "Set 1");
        set1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        set1.setFillColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);

        RadarDataSet set2 = new RadarDataSet(yVals2, "Set 2");
        set2.setColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        set2.setFillColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        set2.setDrawFilled(true);
        set2.setLineWidth(2f);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(xVals, sets);
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
        refreshRequest.timestamp = "2015-08-06 18:05";//dateString;

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

    private class GlucoseLabelPercentages{
        public float Normal;
        public float High;
        public float Dangerous;

        public GlucoseLabelPercentages(float norm, float high, float danger){
            this.Normal = norm;
            this.High = high;
            this.Dangerous = danger;
        }

    }

    private class RefreshPeakTask extends AsyncTask<String, GlucoseLabelPercentages, GlucoseLabelPercentages>{
        /**
         * This method sets up the work to to in the background.
         * */
        @Override
        protected GlucoseLabelPercentages doInBackground(String... strings) {
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

                float Normal = elements.getAsJsonPrimitive("Normal").getAsFloat();
                float High = elements.getAsJsonPrimitive("High").getAsFloat();
                float Dangerous = elements.getAsJsonPrimitive("Dangerous").getAsFloat();

                GlucoseLabelPercentages labels = new GlucoseLabelPercentages(Normal, High, Dangerous);

                return labels;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        protected void onPostExecute(GlucoseLabelPercentages labels) {
            if (labels != null) {
                Toast.makeText(getContext(), "Successfully refreshed glucose peak!", Toast.LENGTH_LONG).show();

                arcView.addEvent(new DecoEvent.Builder(labels.Normal).setIndex(normalSeriesIndex).build());
                arcView.addEvent(new DecoEvent.Builder(labels.High).setIndex(highSeriesIndex).build());
                arcView.addEvent(new DecoEvent.Builder(labels.Dangerous).setIndex(dangerousSeriesIndex).build());
            } else {
                Toast.makeText(getContext(), "Error refreshing peak.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

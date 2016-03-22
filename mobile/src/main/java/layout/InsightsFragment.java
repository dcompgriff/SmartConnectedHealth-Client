package layout;
/**
 * Author: Daniel Griffin
 * */
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
import com.sch.trustworthysystems.smartconnectedhealth_client.MyYAxisValueFormatter;
import com.sch.trustworthysystems.smartconnectedhealth_client.R;
import com.sch.trustworthysystems.smartconnectedhealth_client.view.MyMarkerView;

import java.util.ArrayList;

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

//Try to import one of the plotting libraries.

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link InsightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InsightsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // Labels for synthetic spider chart plot.
    private String[] mParties = new String[]{
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I"
    };

    // UI references.
    private RadarChart mChart;
    private BarChart mBarChart;
    //private Typeface tf;

    public InsightsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InsightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InsightsFragment newInstance(String param1, String param2) {
        InsightsFragment fragment = new InsightsFragment();
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

        // Create the bar graph view, and add it to this fragment's layout.
        createGlucosePeakBarChart();
        // Create the spider view, and add it to this fragment's layout.
        createSpiderView();
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

        setBarChartData(7, 50);

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
            for (int i = 0; i < count; i++) {
                float mult = (range + 1);
                float val = (float) (Math.random() * mult);
                yVals1.add(new BarEntry(val, i));
            }

            // Create an object for holding metadata related to the y-values of the bars.
            BarDataSet set1 = new BarDataSet(yVals1, "Glucose Peaks");
            set1.setBarSpacePercent(35f);
            // Set the color of the bars in the bar chart.
            set1.setColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorPrimary));

            //Add the bars to the set of y-values to be plotted.
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

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

}

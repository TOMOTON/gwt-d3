/**
 * 
 */
package com.github.gwtd3.ui.chart;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Datum;
import com.github.gwtd3.api.core.Formatter;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.scales.Scale;
import com.github.gwtd3.api.svg.Axis;
import com.github.gwtd3.api.svg.Axis.Orientation;
import com.github.gwtd3.ui.event.RangeChangeEvent;
import com.github.gwtd3.ui.event.RangeChangeEvent.RangeChangeHandler;
import com.github.gwtd3.ui.model.AxisModel;
import com.github.gwtd3.ui.svg.GContainer;
import com.github.gwtd3.ui.svg.Text;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Represents a vertical or horizontal axis in a chart,
 * and consists in a line representing domain values,
 * with a title label, and ticks represented.
 * <p>
 * the default style name is ChartAxis and the default style sheet can be found here:
 * 
 * <p>
 * An {@link AxisModel} must be provided to define what values are displayed in ticks.
 * <p>
 * FIXME: add a time formatter
 * 
 * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
 * 
 */
public class ChartAxis<S extends Scale<S>> extends GContainer {

    private static final int DEFAULT_LENGTH = 100;

    private final Axis generator;
    private final AxisModel<S> model;
    private final Text titleLabel;
    private Styles styles;
    private final Orientation tickOrientation;
    private int length = DEFAULT_LENGTH;

    /**
     * Define the styles for the axis.
     * <p>
     * default stylesheet is in ChartAxis.css
     * 
     * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
     * 
     */
    public static interface Resources extends ClientBundle {
        @Source("ChartAxis.css")
        public Styles styles();
    }

    /**
     * Styles to be applied to the axis.
     * default stylesheet is in ChartAxis.css
     * 
     * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
     * 
     */
    public static interface Styles extends CssResource {
        /**
         * @return main class applied to the root axis element.
         */
        public String axis();
    }

    public ChartAxis(final AxisModel<S> model, final Orientation tickOrientation) {
        this(model, tickOrientation, (Resources) GWT.create(Resources.class));
    }

    public ChartAxis(final AxisModel<S> model, final Orientation tickOrientation, final Resources resources) {
        super();
        Preconditions.checkNotNull(model, "please provide a model");
        Preconditions.checkNotNull(tickOrientation, "please provide a tickorientation");
        Preconditions.checkNotNull(resources, "please provide a Resources instance");
        this.model = model;
        model.addRangeChangeHandler(new RangeChangeHandler() {
            @Override
            public void onRangeChange(final RangeChangeEvent event) {
                generator.scale(model.scale());
                scheduleRedraw();
            }
        });
        this.tickOrientation = tickOrientation;
        this.generator = D3.svg().axis().scale(model.scale()).orient(tickOrientation);
        this.titleLabel = createTitle();
        add(titleLabel);
        styles = resources.styles();
    }

    @Override
    protected void onSelectionAttached() {
        super.onSelectionAttached();

        getDocument().inject(styles);
        addStyleName(styles.axis());
    }

    /**
     * @return
     */
    private Text createTitle() {
        Text t = new Text();
        // FIXME: put that in the chart
        // t.addStyleName(chart.styles().label(), true);
        // t.addStyleName(chart.styles().axis(), true);
        t.getElement().getStyle().setProperty("textAnchor", "end");
        if (tickOrientation.isVerticalAxis()) {
            if (tickOrientation == Orientation.RIGHT) {
                t.transform().translate(-23, 0);
            }
            t.transform().rotate(-90);
            t.y("6");
            t.dy(".71em");
        }
        else {
            // position under the tick labels...
            if (tickOrientation == Orientation.TOP) {
                t.transform().translate(length, 18);
            }
            // or at the "end" of the axis?
            // FIXME X label
            // label.classed(chart.styles().x(), true);
        }
        return t;
    }

    /**
     * Provides the generator used to draw the axis for deeper customization.
     * 
     * @return the axis generator
     */
    public Axis generator() {
        return generator;
    }

    public AxisModel<S> model() {
        return model;
    }

    // ======== label ===================

    /**
     * Set the text of the label to be displayed for the axis.
     * 
     * @param text
     *            the text to set, null to remove the axis
     * @return the axis
     */
    public ChartAxis<S> title(final String text) {
        titleLabel.setText(text);
        return this;
    }

    /**
     * @return the titleLabel
     */
    public Text getTitleLabel() {
        return titleLabel;
    }

    // ======== ticks formatter ========
    /**
     * Use the given {@link Formatter} to format the tick labels.
     * 
     * @param formatter
     *            the d3 formatter
     * @return the chart
     */
    public ChartAxis<S> formatter(final Formatter formatter) {
        generator().tickFormat(formatter);
        scheduleRedraw();
        return this;
    }

    // public ChartAxis<S> formatter(final TimeFormat format) {
    // generator().tickFormat((Formatter) format.cast());
    // scheduleRedraw();
    // return this;
    // }

    /**
     * Use the given {@link NumberFormat} to format the tick labels.
     * 
     * @param format
     *            the formatter to be used
     * @return the chart
     */
    public ChartAxis<S> formatter(final NumberFormat format) {
        formatter(new DatumFunction<String>() {
            @Override
            public String apply(final Element context, final Datum d, final int index) {
                String format2 = format.format(d.asDouble());
                return format2;
            }
        });
        return this;
    }

    /**
     * Use the given {@link DatumFunction} to format tick labels, as specified
     * in {@link Axis#tickFormat(DatumFunction)}.
     * 
     * @param formatFunction
     *            the format function to be used
     * @return the chart
     */
    public ChartAxis<S> formatter(final DatumFunction<String> formatFunction) {
        generator().tickFormat(formatFunction);
        scheduleRedraw();
        return this;
    }

    // ======= business =========

    /*
     * (non-Javadoc)
     * 
     * @see com.github.gwtd3.ui.svg.Container#redraw()
     */
    @Override
    public void redraw() {
        super.redraw();
        // update the range of the scale to fit new size if necessary
        if (tickOrientation.isHorizontalAxis()) {
            // LEFT TO RIGHT orientation
            // TODO: let the user decide "right to left"
            model.setPixelRange(0, length);
            generator.scale(model.scale());
            if (tickOrientation == Orientation.TOP) {
                titleLabel.transform().removeAll().translate(length, 18);
            }
            else {
                titleLabel.transform().removeAll().translate(length, -6);
            }
        }
        else {
            // orientation of the domain ("bottom up")
            // TODO: let the user decide a "top down" orientation
            model.setPixelRange(length, 0);
            generator.scale(model.scale());
        }
        // apply the generator on the axis
        select().call(generator);

    }

    /**
     * Set the length in pixel the axis extends.
     * <p>
     * 
     * @param size
     */
    public void setLength(final int length) {
        if (this.length == length) {
            return;
        }
        this.length = length;
        scheduleRedraw();

    }

}

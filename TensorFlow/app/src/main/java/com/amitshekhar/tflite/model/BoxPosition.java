package com.amitshekhar.tflite.model;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.RectF;
import android.util.TypedValue;

import com.amitshekhar.tflite.Config;

/**
 * Model to store the position of the bounding boxes
 *
 * Created by Zoltan Szabo on 1/14/18.
 * URL: https://github.com/szaza/android-yolo-v2
 */

public class BoxPosition {
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float width;
    private float height;

    public BoxPosition(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        init();
    }

    public BoxPosition(BoxPosition boxPosition) {
        this.left = boxPosition.left;
        this.top = boxPosition.top;
        this.width = boxPosition.width;
        this.height = boxPosition.height;

        init();
    }

    public void init() {
        float tmpLeft = this.left;
        float tmpTop = this.top;
        float tmpRight = this.left + this.width;
        float tmpBottom = this.top + this.height;

        this.left = Math.min(tmpLeft, tmpRight); // left should have lower value as right
        this.top = Math.min(tmpTop, tmpBottom);  // top should have lower value as bottom
        this.right = Math.max(tmpLeft, tmpRight);
        this.bottom = Math.max(tmpTop, tmpBottom);
    }

    //function to get original bitmapt ration from rescalec
    public RectF reCalc(BoxPosition rect){
        rect.left = Math.abs(rect.left);
        rect.top =  Math.abs(rect.top);
        rect.right = Math.abs(rect.right);
        rect.bottom = Math.abs(rect.bottom);
        float resultsViewHeight =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                112, Resources.getSystem().getDisplayMetrics());
        int padding = 5;
        float overlayViewHeight = this.getHeight() - resultsViewHeight;
        float sizeMultiplier = Math.min((float) this.getWidth() / (float) Config.INPUT_SIZE,
                overlayViewHeight / (float) Config.INPUT_SIZE);

        float offsetX = (this.getWidth() - Config.INPUT_SIZE * sizeMultiplier) / 2;
        float offsetY = (overlayViewHeight - Config.INPUT_SIZE * sizeMultiplier) / 2 + resultsViewHeight;

        float left = Math.max(padding,sizeMultiplier * rect.getLeft() + offsetX);
        float top = Math.max(offsetY + padding, sizeMultiplier * rect.getTop() + offsetY);

        float right = Math.min(rect.getRight() * sizeMultiplier, this.getWidth() - padding);
        float bottom = Math.min(rect.getBottom() * sizeMultiplier + offsetY, this.getHeight() - padding);

        return new RectF(left, top, right, bottom);
    }
    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    @Override
    public String toString() {
        return "BoxPosition{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}

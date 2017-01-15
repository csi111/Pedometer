/* 
 * NMapViewerResourceProvider.java $version 2010. 1. 1
 * 
 * Copyright 2010 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. 
 */

package com.sean.android.pedometer.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ListView;

import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;
import com.sean.android.pedometer.R;

public class NMapViewerResourceProvider extends NMapResourceProvider
{
	public class NMapPOIflagType
	{
		public static final int UNKNOWN = 0x0000;

		// Single POI icons
		private static final int SINGLE_POI_BASE = 0x0100;

		// Spot, Pin icons
		public static final int SPOT = SINGLE_POI_BASE + 1;
		public static final int PIN  = SPOT + 1;


		// end of single marker icon
		public static final int SINGLE_MARKER_END = 0x04FF;
	}

	private final Paint mTextPaint = new Paint();

	public NMapViewerResourceProvider(Context context) {
		super(context);

		mTextPaint.setAntiAlias(true);
	}

	@Override
	public Drawable getDrawableForInfoLayer(NMapOverlayItem item) {
		return null;
	}

	/**
	 * Class to find resource Ids on map view
	 */
	private class ResourceIdsOnMap {

		int markerId;
		int resourceId;
		int resourceIdFocused;

		ResourceIdsOnMap(int markerId, int resourceId, int resourceIdFocused) {
			this.markerId = markerId;
			this.resourceId = resourceId;
			this.resourceIdFocused = resourceIdFocused;
		}
	}

	// Resource Ids for single icons
	private final ResourceIdsOnMap mResourceIdsForMarkerOnMap[] = {
		// Spot, Pin icons
		new ResourceIdsOnMap(NMapPOIflagType.PIN, R.drawable.ic_pin_01, R.drawable.ic_pin_02),
		new ResourceIdsOnMap(NMapPOIflagType.SPOT, R.drawable.ic_pin_01, R.drawable.ic_pin_02),
	};

	@Override
	protected int findResourceIdForMarker(int markerId, boolean focused) {
		int resourceId = 0;

		if (markerId < NMapPOIflagType.SINGLE_MARKER_END) {
			resourceId = getResourceIdOnMapView(markerId, focused, mResourceIdsForMarkerOnMap);
			if (resourceId > 0) {
				return resourceId;
			}
		}
		return resourceId;
	}

	private int getResourceIdOnMapView(int markerId, boolean focused, ResourceIdsOnMap resourceIdsArray[]) {
		int resourceId = 0;

		for (ResourceIdsOnMap resourceIds : resourceIdsArray) {
			if (resourceIds.markerId == markerId) {
				resourceId = (focused) ? resourceIds.resourceIdFocused : resourceIds.resourceId;
				break;
			}
		}
		return resourceId;
	}

	@Override
	protected void setBounds(Drawable marker, int markerId, NMapOverlayItem item) {
	}

	@Override
	public Drawable[] getLocationDot() {
		return null;
	}

	@Override
	public Drawable getDirectionArrow() {
		return null;
	}

	@Override
	protected Drawable getDrawableForMarker(int markerId, boolean focused, NMapOverlayItem item) {
		return null;
	}

	@Override
	public Drawable getCalloutBackground(NMapOverlayItem item) {
		return null;
	}

	@Override
	public String getCalloutRightButtonText(NMapOverlayItem item) {
		return null;
	}

	@Override
	public Drawable[] getCalloutRightButton(NMapOverlayItem item) {
		return null;
	}

	@Override
	public Drawable[] getCalloutRightAccessory(NMapOverlayItem item) {
		return null;
	}

	@Override
	public int[] getCalloutTextColors(NMapOverlayItem item) {
		return null;
	}

	@Override
	public int getParentLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getOverlappedListViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemLayoutIdForOverlappedListView() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemTextViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemTailTextViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemImageViewId() {
		// not supported
		return 0;
	}

	@Override
	public int getListItemDividerId() {
		// not supported
		return 0;
	}

	@Override
	public void setOverlappedListViewLayout(ListView listView, int itemCount, int width, int height) {
		// not supported
	}

	@Override
	public void setOverlappedItemResource(NMapPOIitem poiItem, ImageView imageView) {
		// not supported
	}
}

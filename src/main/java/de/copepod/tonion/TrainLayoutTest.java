package de.copepod.tonion;
/*
 * Copyright (c) 2014, Birke Heeren All rights reserved.
 * Use only at own risk.
 *
 * TOnion Project
 * Version 2.0: 5 July 2014
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

import javax.swing.JViewport;

/** <code>TrainLayoutTest</code> is part of TOnionLayout. It is used instead
 *  of <code>TrainLayout</code> in order to find inconsistencies in sizes.
 *
 * @author  Birke Heeren
 * @since   private
 * @version TrainLayoutTest 2.0 (released 5. July 2014)
 */
public class TrainLayoutTest extends TrainLayout
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1704298969929018460L;

	private boolean callOnce;

	public TrainLayoutTest(Container self)
	{
		super(self);
		callOnce = true;
	}

	public TrainLayoutTest(Container self, int hgap)
	{
		super(self, hgap);
		callOnce = true;
	}

	 public void layoutContainer(Container self)
	    {
	      synchronized (self.getTreeLock())
	      {
	    	  checkContainer(self);
	    	  int ncomponents = self.getComponentCount();
	    	  if (ncomponents == 0) return;

	        Insets insets = self.getInsets();
	        int h;
	        int w;
	        if (self.getParent() instanceof JViewport)
	        {
	        	JViewport vp = (JViewport)self.getParent();
	        	h = vp.getHeight() - (insets.top + insets.bottom);
	        	w = vp.getWidth() - (insets.left + insets.right) - hgap*(ncomponents-1);
	        }
	        else
	        {
	        	h = self.getHeight() - (insets.top + insets.bottom);
	        	w = self.getWidth() - (insets.left + insets.right) - hgap*(ncomponents-1);
	        }
	        int hmin = 0;
	        int hmax = Integer.MAX_VALUE;
	        int wmintotal = 0;
	        int[] wmin = new int[ncomponents];
	        int[] wmax = new int[ncomponents];
	        for (int i = 0 ; i < ncomponents ; i++)
	        {
	            Component comp = self.getComponent(i);
	            Dimension dmin;
	            Dimension dmax;
	            /* In case Component is Container with Layout instance of TrainLayout or TotemLayout
	             * the dimensions derived by content - if any - should override given Dimensions.
	             * Only when there is no content the given Dimensions should be used.
	             * */
	            if(  comp instanceof Container && ( ((Container)comp).getLayout() instanceof TotemLayout || ((Container)comp).getLayout() instanceof TrainLayout )  )
	        	{
	            	Dimension dminContent = ((LayoutManager2)((Container)comp).getLayout()).minimumLayoutSize((Container)comp);
	        		if (dminContent != null)
	        		{
	        			dmin = dminContent;
	        			if(comp.getMinimumSize() != null && !dmin.equals(comp.getMinimumSize()))
	        			{
	        				System.err.println("TrainLayoutTest [point 1]");
	        				if(callOnce)
	        				{
		        				this.toString(); // ATTENTION
		        				callOnce = false;
	        				}
	        			}
	        		}
	        		else dmin = comp.getMinimumSize();
	        		Dimension dmaxContent = ((LayoutManager2)((Container)comp).getLayout()).maximumLayoutSize((Container)comp);
	        		if (dmaxContent != null)
	        		{
	        			dmax = dmaxContent;
	        			if(comp.getMaximumSize() != null && !dmax.equals(comp.getMaximumSize()))
	        			{
	        				System.err.println("TrainLayoutTest [point 2]");
	        				if(callOnce)
	        				{
	        					this.toString(); // ATTENTION
	        					callOnce = false;
	        				}
	        			}
	        		}
	        		else dmax = comp.getMaximumSize();
	        	}
	            else
	            {
	            	dmin = comp.getMinimumSize();
	                dmax = comp.getMaximumSize();
	            }

	            // MINIMUM
	            if(dmin != null)
	            {
	            	if(dmin.height > hmin) hmin = dmin.height; // minheight is maximized
	            	wmin[i] = dmin.width;
	            	wmintotal += dmin.width;
	            }
	            else // minimum was not set on innermost layer
	            {
	            	wmin[i] = w/ncomponents;
	            	wmintotal += w/ncomponents;
	            }
	            // MAXIMUM
	            if(dmax != null)
	            {

	            	if(dmax.height < hmax) hmax = dmax.height;	// maxheight is minimized
	            	wmax[i] = dmax.width;
	            }
	            else // maximum was not set on innermost layer
	            {
	            	wmax[i] = w/ncomponents;
	            }
	        }

	        // height
	        if(hmin>hmax)
	        {
	        	hmax = hmin; // error correction
	        	System.err.println("TrainLayoutTest [point 3]");
	        	if(callOnce)
	        	{
		        	this.toString(); // ERROR
		        	callOnce = false;
	        	}
	        }
	        else if(hmax != Integer.MAX_VALUE)
	        {
	        	if(h <= hmin)
	        	{
	        		if (h < hmin && !(self.getParent() instanceof JViewport))
	                {
	        			System.err.println("TrainLayoutTest [point 4]");
	        			if(callOnce)
	    	        	{
	        				this.toString(); // ERROR
	        				callOnce = false;
	    	        	}
	                }
	        		h = hmin;
	        	}
	      	  	else if(hmax < h) h = hmax;
	        	// else h = h;
	        }
	        else if(h < hmin)
	        {
	        	if (h < hmin && !(self.getParent() instanceof JViewport))
	            {
	        		System.err.println("TrainLayoutTest [point 5]");
	        		if(callOnce)
	        		{
	        			this.toString(); // ERROR
	        			callOnce = false;
	        		}
	            }
	        	h = hmin;
	        }
	        //else h = h;
	        //
	        // width
	        int[] wfinal = new int[ncomponents];
	        int wcompare = 0;
	        int[] wdifference = new int[ncomponents];
	        int wdifferencetotal = 0;
	        for (int i = 0 ; i < ncomponents ; i++)
	        {
	        	if(wmax[i]<wmin[i])
	      	  	{
	        		wmax[i] = wmin[i]; // error correction
	        		System.err.println("TrainLayoutTest [point 6]");
	        		if(callOnce)
	        		{
	        			this.toString(); // ERROR
	        			callOnce = false;
	        		}
	      	  	}
	      	  	// allocating available width according to minimum widths vs. wmintotal
	        	wfinal[i] = (int)((wmin[i]/(float)wmintotal)*w);
	    		if(wmin[i] > wfinal[i])
	    		{
	    			wfinal[i] = wmin[i];
	    		}
	    		else if(wmax[i] < wfinal[i])
	    		{
	    			wfinal[i] = wmax[i];
	    		}
	    		wcompare += wfinal[i];
	    		wdifference[i] = wmax[i]-wfinal[i];
	    		wdifferencetotal += wdifference[i];
	        }
	        int wleftover = w - wcompare;
	        // dispensing possible wleftover according to wdifference vs. wdifferencetotal
	        if(wleftover > 0)
	        {
	            wcompare = 0;
	            for (int i = 0 ; i < ncomponents ; i++)
	            {
	        		wfinal[i] += (int)((wdifference[i]/(float)wdifferencetotal)*wleftover);
	            	if(wmax[i] < wfinal[i])
	            	{
	            		wfinal[i] = wmax[i];
	            	}
	            	wcompare += wfinal[i];
	            }
	        }
	        wleftover = w - wcompare;
	        // dispensing possible wleftover from back to front
	        if(wleftover > 0)
	        {
	      	  for (int i = ncomponents-1; i >= 0; i--)
	      	  {
	      		  int wdiff = wmax[i] - wfinal[i];
	      		  if(wdiff > 0 &&  wdiff < wleftover)
	      		  {
	      			  wfinal[i] = wmax[i];
	      			  wleftover -= wdiff;
	      		  }
	      		  else if(wdiff > 0)
	      		  {
	      			  wfinal[i] += wleftover;
	      			  break;
	      		  }
	      	  }
	        }

	        wcompare = 0;
	          for (int i = 0 ; i < ncomponents ; i++)
	          {
	        	  if(wmin[i] > wfinal[i])
	        	  {
	        		  System.err.println("TrainLayoutTest [point 7]");
	        		  if(callOnce)
	        		  {
		        		  this.toString();
		        		  callOnce = false;
	        		  }
	        	  }
	        	  wcompare += wfinal[i];
	          }
	          if(wcompare < w)
	          {
	        	  System.err.println("TrainLayoutTest [point 8]");
	        	  if(callOnce)
	        	  {
		        	  this.toString();
		        	  callOnce = false;
	        	  }
	          }

	        int x = insets.left;
	        for (int i = 0 ; i < ncomponents ; i++)
	        {
	            Component comp = self.getComponent(i);
	            comp.setBounds(x, insets.top, wfinal[i], h);
	            x += wfinal[i] + hgap;
	        }
	      }
	    }

}


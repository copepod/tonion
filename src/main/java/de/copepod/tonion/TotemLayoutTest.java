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

/** <code>TotemLayoutTest</code> is part of TOnionLayout. It is used instead
 *  of <code>TotemLayout</code> in order to find inconsistencies in sizes.
 *
 * @author  Birke Heeren
 * @since   private
 * @version TotemLayoutTest 2.0 (released 5. July 2014)
 */
public class TotemLayoutTest extends TotemLayout
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6345429179744253717L;

	private boolean callOnce;

	public TotemLayoutTest(Container self)
	{
		super(self);
		callOnce = true;
	}

	public TotemLayoutTest(Container self, int vgap)
	{
		super(self, vgap);
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
            int w;
            int h;
            if (self.getParent() instanceof JViewport)
            {
            	JViewport vp = (JViewport)self.getParent();
            	w = vp.getWidth() - (insets.left + insets.right);
            	h = vp.getHeight() - (insets.top + insets.bottom) - vgap*(ncomponents-1);
            }
            else
            {
            	w = self.getWidth() - (insets.left + insets.right);
            	h = self.getHeight() - (insets.top + insets.bottom) - vgap*(ncomponents-1);
            }
            int wmin = 0;
            int wmax = Integer.MAX_VALUE;
            int hmintotal = 0;
            int[] hmin = new int[ncomponents];
            int[] hmax = new int[ncomponents];
            for (int i = 0 ; i < ncomponents ; i++)
            {
                Component comp = self.getComponent(i);
                Dimension dmin;
                Dimension dmax;
                /* In case Component is Container with Layout instance of TrainLayout or TotemLayout
                 * the dimensions derived by content - if any - should override given Dimensions.
                 * Only when there is no content the given Dimensions should be used.
                 * */
                if(  comp instanceof Container && ( ((Container)comp).getLayout() instanceof TrainLayout || ((Container)comp).getLayout() instanceof TotemLayout )  )
                {
                	Dimension dminContent = ((LayoutManager2)((Container)comp).getLayout()).minimumLayoutSize((Container)comp);
            		if (dminContent != null)
            		{
            			dmin = dminContent;
            			if(comp.getMinimumSize() != null && !dmin.equals(comp.getMinimumSize()))
	        			{
            				System.err.println("TotemLayoutTest [point 1]");
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
	        				System.err.println("TotemLayoutTest [point 2]");
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
                	if(dmin.width > wmin) wmin = dmin.width;	// minwidth is maximized
                	hmin[i] = dmin.height;
                	hmintotal += dmin.height;
                }
                else // minimum was not set on innermost layer
                {
                	hmin[i] = h/ncomponents;
                	hmintotal += h/ncomponents;
                }
                // MAXIMUM
                if(dmax != null)
                {

                	if(dmax.width < wmax) wmax = dmax.width;	// maxwidth is minimized
                	hmax[i] = dmax.height;
                }
                else
                {
                	hmax[i] = h/ncomponents;
                }
            }
            // width
            if(wmin>wmax)
            {
            	wmax = wmin; // error correction
            	System.err.println("TotemLayoutTest [point 3]");
            	if(callOnce)
            	{
            		this.toString(); // ERROR
            		callOnce = false;
            	}
            }
            else if(wmax != Integer.MAX_VALUE)
            {
            	if(w <= wmin)
            	{
            		if (w < wmin && !(self.getParent() instanceof JViewport))
	                {
	        			System.err.println("TotemLayoutTest [point 4]");
	        			if(callOnce)
	        			{
	        				this.toString(); // ERROR
	        				callOnce = false;
	        			}
	                }
            		w = wmin;
            	}
            	else if(wmax < w) w = wmax;
            	// else w = w;
            }
            else if(w < wmin)
            {
            	if (w < wmin && !(self.getParent() instanceof JViewport))
	            {
	        		System.err.println("TotemLayoutTest [point 5]");
	        		if(callOnce)
	        		{
		        		this.toString(); // ERROR
		        		callOnce = false;
	        		}
	            }
            	w = wmin;
            }
            //else w = w;
            //
            // height
            int[] hfinal = new int[ncomponents];
            int hcompare = 0;
            int[] hdifference = new int[ncomponents];
            int hdifferencetotal = 0;
            for (int i = 0 ; i < ncomponents ; i++)
            {
            	if(hmax[i]<hmin[i])
            	{
            		hmax[i]=hmin[i];
            		System.err.println("TotemLayoutTest [point 6]");
            		if(callOnce)
            		{
    	        		this.toString(); // ERROR
    	        		callOnce = false;
            		}
            	}
          	  	// allocating available height according to minimum heights vs. hmintotal
            	hfinal[i] = (int)((hmin[i]/(float)hmintotal)*h);
        		if(hmin[i] > hfinal[i])
        		{
        			hfinal[i] = hmin[i];
        		}
        		else if(hmax[i] < hfinal[i])
        		{
        			hfinal[i] = hmax[i];
        		}
        		hcompare += hfinal[i];
        		hdifference[i] = hmax[i]-hfinal[i];
        		hdifferencetotal += hdifference[i];
            }
            int hleftover = h - hcompare;
            // dispensing possible hleftover according to hdifference vs. hdifferencetotal
            if(hleftover > 0)
            {
                hcompare = 0;
                for (int i = 0 ; i < ncomponents ; i++)
                {
            		hfinal[i] += (int)((hdifference[i]/(float)hdifferencetotal)*hleftover);
                	if(hmax[i] < hfinal[i])
                	{
                		hfinal[i] = hmax[i];
                	}
                	hcompare += hfinal[i];
                }
            }
            hleftover = h - hcompare;
            // dispensing possible hleftover from back to front
            if(hleftover > 0)
            {
          	  for (int i = ncomponents-1; i >= 0; i--)
          	  {
          		  int hdiff = hmax[i] - hfinal[i];
          		  if(hdiff > 0 &&  hdiff < hleftover)
          		  {
          			  hfinal[i] = hmax[i];
          			  hleftover -= hdiff;
          		  }
          		  else if(hdiff > 0)
          		  {
          			  hfinal[i] += hleftover;
          			  break;
          		  }
          	  }
            }


            hcompare = 0;
	          for (int i = 0 ; i < ncomponents ; i++)
	          {
	        	  if(hmin[i] > hfinal[i])
	        	  {
	        		  System.err.println("TotemLayoutTest [point 7]");
	        		  if(callOnce)
	        		  {
		        		  this.toString();
		        		  callOnce = false;
	        		  }
	        	  }
	        	  hcompare += hfinal[i];
	          }
	          if(hcompare < h)
	          {
	        	  System.err.println("TotemLayoutTest [point 8]");
	        	  if(callOnce)
	        	  {
		        	  this.toString();
		        	  callOnce = false;
	        	  }
	          }


            int y = insets.top;
            for (int i = 0 ; i < ncomponents ; i++)
            {
                Component comp = self.getComponent(i);
                comp.setBounds(insets.left, y, w, hfinal[i]);
                y += hfinal[i] + vgap;
            }
        }
    }

}


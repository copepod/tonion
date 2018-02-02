package de.copepod.tonion;
/*
 * Copyright (c) 2014, Birke Heeren All rights reserved.
 * Use only at own risk.
 *
 * TOnion Project
 * Version 2.0: 5 July 2014
 */


import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;

import javax.swing.JViewport;


/**
 * The <code>TotemLayout</code> class is a layout manager that
 * lays out a container's components in one column from top to bottom.
 * <p>
 * Minimum and maximum sizes are taken into account.
 * <p>
 * <code>TotemLayout</code>and <code>TrainLayout</code> work together like layers
 * of an onion. They stack into each other and are called TOnionLayout.
 * TOnionLayout was developed to layout forms and datamasks. By using minimum and
 * maximum size the layout will resize to fit the available space.
 * The components inside TOnionLayout only have to fit together approximately, the
 * layout will align the components to look neatly by itself. <code>TotemLayout</code> will give
 * all components the same width and optimize the height of each component.
 * <p>
 * Even though TOnionLayout is done top-down each layer inquires about the minimum
 * and maximum sizes of all its components. To acquire a good performance each layer
 * caches the overall minimum and maximum size of its components. Therefore TotemLayout
 * can not be shared. Adding or removing a component invalidates the cache of
 * the layout and all TOnion layouts above it.
 * <p>
 * All first components inside a TOnionLayout must have a minimum and maximum
 * size set for the layout to function properly, otherwise minimum and maximum
 * sizes are estimated.
 * TOnionLayers that change between filled and empty should have a minimum and
 * maximum size set, which is only used when empty.
 * <p>
 * JButtons should be wrapped with a JPanel that has a FlowLayout. The
 * minimum and maximum sizes are set on the JPanel.
 * <p>
 * JTables should be wrapped with a JPanel that has a BorderLayout and
 * be added to the center component. The minimum and maximum sizes are
 * set on the JPanel.
 * <p>
 * TOnionLayout can be placed inside a JScrollPane. If the window
 * size is deceased TOnionLayout will shrink to its minimum size before the scrollbars
 * appear.
 * <p>
 * TOnionLayout corrects inconsistencies of minimum and maximum sizes with
 * maximum = minimum; Use <code>TotemLayoutTest</code> to show inconsistencies
 * or method toString().
 *
 * @author  Birke Heeren
 * @since   private
 * @version TotemLayout 2.0 (released 5. July 2014)
 */
public class TotemLayout implements LayoutManager, LayoutManager2, java.io.Serializable
{
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -7411804673224730902L;

    /**
     * This is the vertical gap (in pixels) which specifies the space
     * between items.  They can be changed at any time.
     * This should be a non negative integer.
     *
     * @serial
     * @see #getVgap()
     * @see #setVgap(int)
     */
    protected int vgap;

    /**
     * TotemLayout remembers the minimum size of its components. Adding or
     * deleting a component causes the minimum size to be recalculated. The
     * update is passed up the TOnion layers to the outside, therefore
     * TotemLayout must know the component it is assigned to. TotemLayout
     * can not be shared between components.
     */
    private Dimension dimMin;

    /**
     * TotemLayout remembers the maximum size of its components. Adding or
     * deleting a component causes the maximum size to be recalculated. The
     * update is passed up the TOnion layers to the outside, therefore
     * TrainLayout must know the component it is assigned to. TotemLayout
     * can not be shared between components.
     */
    private Dimension dimMax;

    /**
     * This is the container TotemLayout is assigned to.
     */
    private Container self;



    /**
     * Creates a totem layout with a default vertical gap.
     * @since private
     */
    public TotemLayout(Container self)
    {
        this(self, 0);
    }

    /**
     * Creates a totem layout with the specified vertical gap.
     * <p>
     * All <code>TotemLayout</code> constructors defer to this one.
     * @param     vgap   the vertical gap
     * @exception   IllegalArgumentException  if the value of the
     * 				vertical gap is less than zero.
     */
    public TotemLayout(Container self, int vgap)
    {
        if (vgap<0) throw new IllegalArgumentException("the vertical gap can not be a negativ number");
        this.vgap = vgap;
        this.dimMin = null;
        this.dimMax = null;
        this.self = self;
    }

    /**
     * Gets the vertical gap between components.
     * @return       the vertical gap between components
     * @since        private
     */
    public int getVgap()
    {
        return vgap;
    }

    /**
     * Sets the vertical gap between components to the specified value.
     * @param         vgap  the vertical gap between components
     * @since        private
     */
    public void setVgap(int vgap)
    {
    	if (vgap<0) throw new IllegalArgumentException("the vertical gap can not be a negativ number");
        this.vgap = vgap;
    }

    /**
     * Determines the preferred size of the container argument using
     * this totem layout.
     * <p>
     * The preferred size is all size available.
     *
     * @param     self   the container in which to do the layout
     * @return    the preferred dimensions to lay out the
     *                      subcomponents of the specified container
     * @see       java.awt.Container#getPreferredSize()
     */
    public Dimension preferredLayoutSize(Container self)
    {
    	synchronized (self.getTreeLock())
        {
            int ncomponents = self.getComponentCount();
            if(ncomponents == 0)
            {
            	checkContainer(self);
                if (self.getMinimumSize() != null)
                {
                	return self.getMinimumSize();
                }
                else if (self.getParent() instanceof JViewport)
                {
                	JViewport vp = (JViewport)self.getParent();
                	Insets insets = self.getInsets();
                	return new Dimension(vp.getWidth()-insets.left-insets.right, vp.getHeight()-insets.top-insets.bottom);
                }
                else
                {
                	return self.getSize();
                }
            }

            if (self.getParent() instanceof JViewport)
            {
          	  return this.minimumLayoutSize(self);
            }

            Insets insets = self.getInsets();
            int w;
            int h;

            w = self.getWidth() - (insets.left + insets.right);
            h = self.getHeight() - (insets.top + insets.bottom) - vgap*(ncomponents-1);

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
            		if (dminContent != null) dmin = dminContent;
            		else dmin = comp.getMinimumSize();
            		Dimension dmaxContent = ((LayoutManager2)((Container)comp).getLayout()).maximumLayoutSize((Container)comp);
            		if (dmaxContent != null) dmax = dmaxContent;
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
                else // maximum was not set on innermost layer
                {
                	hmax[i] = h/ncomponents;
                }
            }
            // width
            if(wmin>wmax)
            {
            	// error correction, to show error use TotemLayoutTest
            	wmax = wmin;
            }
            else if(wmax != Integer.MAX_VALUE)
            {
            	if(w <= wmin) w = wmin;
            	else if(wmax < w) w = wmax;
            	// else w = w;
            }
            else if(w < wmin) w = wmin;
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
            		// error correction, to show error use TotemLayoutTest
            		hmax[i]=hmin[i];
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

            int hfinaltotal = insets.top;
            for (int i = 0 ; i < ncomponents ; i++)
            {
            	hfinaltotal += hfinal[i] + vgap;
            }
            return new Dimension(hfinaltotal, w);
        }
    }

    /**
     * Determines the minimum size of the container argument using this
     * totem layout.
     * <p>
     * The minimum width of a totem layout is the largest minimum width
     * of all of the components in the container,
     * plus the left and right insets of the self container.
     * <p>
     * The minimum height of a totem layout is the sum of minimum heights
     * of all of the components in the container, plus the vertical
     * padding times the number of items minus one, plus the top and
     * bottom insets of the self container.
     *
     * @param       self   the container in which to do the layout
     * @return      the minimum dimensions needed to lay out the
     *                      subcomponents of the specified container
     * @see         java.awt.Container#doLayout
     */
    public Dimension minimumLayoutSize(Container self)
    {
        synchronized (self.getTreeLock())
        {
        	checkContainer(self);
        	if(dimMin != null) return dimMin;
            int ncomponents = self.getComponentCount();
            if(ncomponents>0)
            {
            	Insets insets = self.getInsets();
          	  	int w = 0;
                int h = 0;
                for (int i = 0 ; i < ncomponents ; i++)
                {
                    Component comp = self.getComponent(i);
                    Dimension dmin;
                    /* In case Component is Container with Layout instance of TrainLayout or TotemLayout
                     * the dimensions derived by content - if any - should override given Dimensions.
                     * Only when there is no content the given Dimensions should be used.
                     * */
                    if(  comp instanceof Container && ( ((Container)comp).getLayout() instanceof TrainLayout || ((Container)comp).getLayout() instanceof TotemLayout )  )
                    {
                    	Dimension dminContent = ((LayoutManager2)((Container)comp).getLayout()).minimumLayoutSize((Container)comp);
                		if (dminContent != null) dmin = dminContent;
                		else dmin = comp.getMinimumSize();
                    }
                    else
                    {
                    	dmin = comp.getMinimumSize();
                    }
                    if(dmin!=null)
                    {
                        if(w < dmin.width) w = dmin.width; // minwidth is maximized
                        h += dmin.height;
                    }
                    else
                    {
                    	h += (self.getHeight()-(insets.top+insets.bottom))/ncomponents;
                    }
                }

                dimMin = new Dimension(insets.left + insets.right + w,
                                     insets.top + insets.bottom + h + (ncomponents-1)*vgap);
                return dimMin;
            }
            dimMin = null;
            return null;
        }
    }

    /**
     * Determines the maximum size of the container argument using this
     * totem layout.
     * <p>
     * The maximum width of a totem layout is the smallest maximum width
     * of all of the components in the container,
     * plus the left and right insets of the self container.
     * <p>
     * The maximum height of a totem layout is the sum of maximum heights
     * of all of the components in the container,
     * plus the vertical padding times the number of items minus one,
     * plus the top and bottom insets of the self container.
     *
     * @param       self   the container in which to do the layout
     * @return      the maximum dimensions needed to lay out the
     *                      subcomponents of the specified container
     * @see         java.awt.Container#doLayout
     */
	@Override
	public Dimension maximumLayoutSize(Container self)
	{
        synchronized (self.getTreeLock())
        {
        	checkContainer(self);
        	if(dimMax != null) return dimMax;
            int ncomponents = self.getComponentCount();
            if(ncomponents>0)
            {
                Insets insets = self.getInsets();
                int w = Integer.MAX_VALUE;
                int h = 0;
                for (int i = 0 ; i < ncomponents ; i++)
                {
                    Component comp = self.getComponent(i);
                    Dimension dmax;
                    /* In case Component is Container with Layout instance of TrainLayout or TotemLayout
                     * the dimensions derived by content - if any - should override given Dimensions.
                     * Only when there is no content the given Dimensions should be used.
                     * */
                    if(  comp instanceof Container && ( ((Container)comp).getLayout() instanceof TrainLayout || ((Container)comp).getLayout() instanceof TotemLayout )  )
                    {
                    	Dimension dmaxContent = ((LayoutManager2)((Container)comp).getLayout()).maximumLayoutSize((Container)comp);
                		if (dmaxContent != null) dmax = dmaxContent;
                		else dmax = comp.getMaximumSize();
                    }
                    else
                    {
                    	dmax = comp.getMaximumSize();
                    }
                    if(dmax!=null)
                    {
                        if(w > dmax.width) w = dmax.width; // maxwidth is minimized
                        h += dmax.height;
                    }
                    else
                    {
                    	h += (self.getHeight()-(insets.top+insets.bottom))/ncomponents;
                    }
                }
                dimMax = new Dimension(insets.left + insets.right + w,
                        			 insets.top + insets.bottom + h + (ncomponents-1)*vgap);
                return dimMax;
            }
            dimMax = null;
            return null;
        }
	}

    /**
     * Lays out the specified container using this layout.
     * <p>
     * This method reshapes the components in the specified self
     * container in order to satisfy the constraints of the
     * <code>TotemLayout</code> object.
     * <p>
     * All components in a totem layout are given the same width,
     * which is equal to the available space minus parent insets.
     * If the smallest maximum width is less, than this overrides
     * the width. Else if the largest minimum width is more, than
     * this overrides the width.
     * <p>
     * The totem layout manager divides the available height
     * according to the relative sizes of the minimum heights if
     * available, empty minimum heights are substituted by the
     * preferred height if available, otherwise the 1/nth height
     * is used for relative sizing. The minimum and maximum
     * heights of each item are observed.
     *
     * @param      self   the container in which to do the layout
     * @see        java.awt.Container
     * @see        java.awt.Container#doLayout
     */
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
            		if (dminContent != null) dmin = dminContent;
            		else dmin = comp.getMinimumSize();
            		Dimension dmaxContent = ((LayoutManager2)((Container)comp).getLayout()).maximumLayoutSize((Container)comp);
            		if (dmaxContent != null) dmax = dmaxContent;
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
            	// error correction, to show error use TotemLayoutTest or toString()
            	wmax = wmin;
            }
            else if(wmax != Integer.MAX_VALUE)
            {
            	if(w <= wmin) w = wmin;
            	else if(wmax < w) w = wmax;
            	// else w = w;
            }
            else if(w < wmin) w = wmin;
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
            		// error correction, to show error use TotemLayoutTest or toString
            		hmax[i]=hmin[i];
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

            int y = insets.top;
            for (int i = 0 ; i < ncomponents ; i++)
            {
                Component comp = self.getComponent(i);
                comp.setBounds(insets.left, y, w, hfinal[i]);
                y += hfinal[i] + vgap;
            }
        }
    }

    /**
     * Returns the string representation of this totem layout's values.
     * @return     a string representation of this totem layout
     */
    public String toString()
    {
    	synchronized (self.getTreeLock())
        {
    		checkContainer(self);
            int ncomponents = self.getComponentCount();
            if (ncomponents == 0)
            {
            	return "TotemLayout has no components.\n"+
     				   "Layout MinimumSize = "+self.getMinimumSize()+
     				   " Layout MaximumSize = "+self.getMaximumSize()+
     				   "\nTotemLayout toString() was called";
            }
            System.out.println("TotemLayout has " + ncomponents + " components.");

            Insets insets = self.getInsets();
            int w;
            int h;
            if (self.getParent() instanceof JViewport)
            {
            	System.out.println("The parent of this layer is a JViewport (usually part of JScrollpane).");
            	JViewport vp = (JViewport)self.getParent();
            	w = vp.getWidth() - (insets.left + insets.right);
            	System.out.println("The width available to the layout is = "+w);
            	h = vp.getHeight() - (insets.top + insets.bottom) - vgap*(ncomponents-1);
            	System.out.println("The height available to the layout is = "+h);
            }
            else
            {
            	System.out.println("The parent of this layer is "+ (self.getParent() != null? self.getParent().getClass():"no parent"));
            	w = self.getWidth() - (insets.left + insets.right);
            	System.out.println("The width available to the layout is = "+w);
            	h = self.getHeight() - (insets.top + insets.bottom) - vgap*(ncomponents-1);
            	System.out.println("The height available to the layout is = "+h);
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
             				 System.out.println("ATTENTION: In component "+i+" the MinimumSize explicitly set was overridden"
             						 +"\non purpose because the component has got TOnionLayout and has got components."
             						 + "\nMinimumSize explicitly set = "+comp.getMinimumSize());
            		}
            		else dmin = comp.getMinimumSize();
            		Dimension dmaxContent = ((LayoutManager2)((Container)comp).getLayout()).maximumLayoutSize((Container)comp);
            		if (dmaxContent != null)
            		{
            			dmax = dmaxContent;
            			if(comp.getMaximumSize() != null && !dmax.equals(comp.getMaximumSize()))
            			System.out.println("ATTENTION: In component "+i+" the MaximumSize explicitly set was overridden"
        						 +"\non purpose because the component has got TOnionLayout and has got components."
        						 +"\nMaximumSize explicitly set = "+comp.getMaximumSize());
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
                	System.out.println("Component "+i+" MinimumWidth = "+dmin.width+", MinimumHeight = "+dmin.height);
                	if(dmin.width > wmin) wmin = dmin.width;	// minwidth is maximized
                	hmin[i] = dmin.height;
                	hmintotal += dmin.height;
                }
                else // minimum was not set on innermost layer
                {
                	hmin[i] = h/ncomponents;
                	hmintotal += h/ncomponents;
                	System.err.println("Component MinimumSize was not set! Estimation for "+i+" MinimumHeight = "+hmin[i]);
                }
                // MAXIMUM
                if(dmax != null)
                {
                	System.out.println("Component "+i+" MaximumWidth = "+dmax.width+", MaximumHeight = "+dmax.height);
                	if(dmax.width < wmax) wmax = dmax.width;	// maxwidth is minimized
                	hmax[i] = dmax.height;
                }
                else // maximum was not set on innermost layer
                {
                	hmax[i] = h/ncomponents;
                	System.err.println("Component MaximumSize was not set! Estimation for "+i+" MaximumHeight = "+hmax[i]);
                }
            }


            // width
            if(wmin>wmax)
            {
            	System.err.println("ERROR in component widths of this layout:"
     					 + "\nThe MinimumWidth required by components = "+wmin
     					 + "\nis larger than"
     					 + "\nthe MaximumWidth allowed by the components = "+wmax);
            	wmax = wmin; // error correction
            }
            else if(wmax != Integer.MAX_VALUE)
            {
            	if(w <= wmin)
            	{
            		if (w < wmin && self.getParent() instanceof JViewport)
                    {
              		  System.out.println("OKAY: JViewport (usually part of JScrollPane) should show horizontal scrollbar"
              		  		+ "\nbecause the width available = "+w
              		  		+"\nis smaller than the MinimumWidth required by the components = "+wmin);
                    }
              		else if (w < wmin)
              		{
              			System.err.println("ERROR: The width available = "+w
        			  			+"\nis smaller than the MinimumWidth required by the components = "+ wmin
        			  			+"\nTherefore part of the components will be hidden!");
              		}
            		w = wmin;
            		System.out.println("component widths are OKAY:"
       					   + "\nThe MinimumWidth required by components = "+wmin
       					   + "\nis smaller or equal to"
       					   + "\nthe MaximumWidth allowed by the components = "+wmax);
            	}
            	else if(wmax < w)
            	{
            		System.out.println("component widths are OKAY:"
        					   + "\nThe MinimumWidth required by components = "+wmin
        					   + "\nis smaller or equal to"
        					   + "\nthe MaximumWidth allowed by the components = "+wmax
        					   + "\nThe width available = "+w
        					   + "\nThe width of the layout is set to = "+wmax);
            		w = wmax;
            	}
            	else // w = w;
            	{
            		System.out.println("component widths in this layout are OKAY:"
     					   + "\nThe MinimumWidth required by components = "+wmin
     					   + "\nis smaller or equal to"
     					   + "\nthe MaximumWidth allowed by the components = "+wmax
     					   + "The width available = "+w
     					   + "\nThe width of the layout is set to = "+w);
            	}
            }
            else if(w < wmin)
            {
            	if (self.getParent() instanceof JViewport)
                {
          		  System.out.println("OKAY: JViewport (usually part of JScrollPane) should show horizontal scrollbar"
          		  		+ "\nbecause the width available = "+w
          		  		+"\nis smaller than the MinimumWidth required by the components = "+wmin);
                }
            	else
            	{
          		  System.err.println("ERROR: The width available = "+w
          				  			+"\nis smaller than the MinimumWidth required by the components = "+ wmin
          				  			+"Therefore part of the components will be hidden!");
            	}
            	w = wmin;
            	System.out.println("component widths are OKAY:"
  					   + "\nThe MinimumWidth required by components = "+wmin
  					   + "\nThe MaximumWidth was not set by the components."
  					   + "\nThe width of the layout is set to = "+w);
            }
            else // w = w;
            {
            	System.out.println("component widths are OKAY:"
 					   + "\nThe MinimumWidth required by components = "+wmin
 					   + "\nThe MaximumWidth was not set by the components."
 					   + "\nThe width of the layout is set to = "+w);
            }
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
            		System.err.println("ERROR in component "+i+" MinimumHeight = "+hmin[i]
              				+"\nis larger than"
              				+"\nMaximumHeight = "+hmax[i]);
            		hmax[i]=hmin[i]; // error correction
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
          			System.err.println("ERROR in component "+i+" MinimumHeight = "+hmin[i]
              				+"\nis larger than"
              				+"\navailable height = "+hfinal[i]
              				+ "\npart of the component will be invisible");
          	  }
          	  hcompare += hfinal[i];
            }
            if(hcompare < h)
          	  System.err.println("ERROR: layout height = "+hcompare
          			  +"\nis smaller than"
          			  + "\navailable height = "+h
          			  + "\ntherefore alignment is broken");


            int y = insets.top;
            for (int i = 0 ; i < ncomponents ; i++)
            {
            	System.out.println("Component "+i+" is set to height = "+hfinal[i]+" and width = "+w);
           	  	System.out.println("Location is x = "+insets.left+" and y = "+y);
                y += hfinal[i] + vgap;
            }
        }
    	return "\nTotemLayout toString() was called";
    }

    /** invalidates Layout, minimum and maximum sizes
     *  of content will be recalculated
     *
     * @param name the name of the component
     * @param comp the component to be added
     */
    @Override
    public void addLayoutComponent(String name, Component comp)
    {
    	invalidateLayout(comp.getParent());
    }

    /** invalidates Layout, minimum and maximum sizes
     *  of content will be recalculated
     *
     * @param name the name of the component
     * @param comp the component to be added
     */
    @Override
	public void addLayoutComponent(Component comp, Object constraints)
	{
    	invalidateLayout(comp.getParent());
	}

    /** invalidates Layout, minimum and maximum sizes
     *  of content will be recalculated
     *
     * @param comp the component to be removed
     */
    @Override
    public void removeLayoutComponent(Component comp)
    {
    	invalidateLayout(comp.getParent());
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    @Override
	public void invalidateLayout(Container self)
	{
		checkContainer(self);
    	this.dimMin = null;
		this.dimMax = null;
		if( self.getParent() != null &&
			self.getParent().getLayout() !=  null &&
			(self.getParent().getLayout() instanceof TrainLayout || self.getParent().getLayout() instanceof TotemLayout) )
		{
			( (LayoutManager2)self.getParent().getLayout() ).invalidateLayout(self.getParent());
		}
	}

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
	@Override
	public float getLayoutAlignmentX(Container self)
	{
		return 0;
	}

	/**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
	@Override
	public float getLayoutAlignmentY(Container self)
	{
		return 0;
	}

	void checkContainer(Container self)
	{
        if (this.self != self)
        {
            throw new AWTError("TotemLayout can't be shared");
        }
    }
}


package de.copepod.tonion;
/*
 * Copyright (c) 2020, Birke Heeren All rights reserved.
 * Use only at own risk.
 *
 * TOnion Project
 * Version 3.0: 20 July 2020
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
 * The <code>TrainLayout</code> class is a layout manager that lays out a
 * container's components in one row from left to right.
 * <p>
 * Minimum and maximum sizes are taken into account.
 * <p>
 * <code>TrainLayout</code>, <code>TotemLayout</code> and
 * <code>BullsEyeLayout</code> work together like layers of an onion. They stack
 * into each other and are called TOnionLayout. TOnionLayout was developed to
 * layout forms and data masks. By using minimum and maximum size the layout will
 * resize to fit the available space. The components inside TOnionLayout only
 * have to fit together approximately, the layout will align the components to
 * look neatly by itself. <code>TrainLayout</code> will give all components the
 * same height and optimize the width of each component.
 * <p>
 * Even though TOnionLayout is done top-down each layer inquires about the
 * minimum and maximum sizes of all its components. To acquire a good
 * performance each layer caches the overall minimum and maximum size of its
 * components. Therefore TrainLayout can not be shared. Adding or removing a
 * component invalidates the cache of the layout and all TOnion layouts above
 * it.
 * <p>
 * All first components inside a TOnionLayout must have a minimum and maximum
 * size set for the layout to function properly, otherwise minimum and maximum
 * sizes are estimated. TOnionLayers that change between filled and empty should
 * have a minimum and maximum size set, which is only used when empty.
 * <p>
 * JButtons should be wrapped with a JPanel that has a FlowLayout. The minimum
 * and maximum sizes are set on the JPanel.
 * <p>
 * JTables should be wrapped with a JPanel that has a BorderLayout and be added
 * to the center component. The minimum and maximum sizes are set on the JPanel.
 * <p>
 * TOnionLayout can be placed inside a JScrollPane. If the window size is
 * deceased TOnionLayout will shrink to its minimum size before the scroll bars
 * appear.
 * <p>
 * TOnionLayout corrects inconsistencies of minimum and maximum sizes with
 * maximum = minimum;
 *
 * @author Birke Heeren
 * @since private
 * @version TrainLayout 3.0 (released 20. July 2020)
 */
public class TrainLayout
      implements LayoutManager, LayoutManager2, java.io.Serializable
{
   /*
    * serialVersionUID
    */
   private static final long serialVersionUID = -7411804673224730903L;

   /**
    * This is the horizontal gap (in pixels) which specifies the space between
    * items. They can be changed at any time. This should be a non negative
    * integer.
    *
    * @serial
    * @see #getHgap()
    * @see #setHgap(int)
    */
   protected int hgap;

   /**
    * TrainLayout remembers the minimum size of its components. Adding or
    * deleting a component causes the minimum size to be recalculated. The
    * update is passed up the TOnion layers to the outside, therefore
    * TrainLayout must know the component it is assigned to. TrainLayout can not
    * be shared between components.
    */
   private Dimension dimMin;

   /**
    * TrainLayout remembers the maximum size of its components. Adding or
    * deleting a component causes the maximum size to be recalculated. The
    * update is passed up the TOnion layers to the outside, therefore
    * TrainLayout must know the component it is assigned to. TrainLayout can not
    * be shared between components.
    */
   private Dimension dimMax;

   /**
    * This is the container TrainLayout is assigned to.
    */
   private Container self;
   
   /**
    * This is a name for test mode.
    */
   private String testname;

   /**
    * This determines test mode or not.
    */
   private LayoutMode mode;

   /**
    * Creates a train layout with no horizontal gap.
    * <p>
    * 
    * @param self
    *           the container to be laid out
    */
   public TrainLayout(Container self)
   {
      this(self, 0, "none", LayoutMode.NOTEST);
   }
   
   /**
    * Creates a train layout with no horizontal gap in test mode.
    * <p>
    * 
    * @param self
    *           the container to be laid out
    * @param testname
    *           the name of the object in test mode
    */
   public TrainLayout(Container self, String testname)
   {
      this(self, 0, testname, LayoutMode.TEST_TRAIN);
   }

   /**
    * Creates a train layout with the specified horizontal gap.
    * <p>
    * 
    * @param self
    *           the container to be laid out
    * @param hgap
    *           the horizontal gap
    * @exception IllegalArgumentException
    *               if the value of the horizontal gap is less than zero.
    */
   public TrainLayout(Container self, int hgap)
   {
      this(self, hgap, "none", LayoutMode.NOTEST);
   }
   
   /**
    * Creates a train layout with the specified horizontal gap in test mode.
    * <p>
    * 
    * @param self
    *           the container to be laid out
    * @param hgap
    *           the horizontal gap
    * @param testname
    *           the name of the object in test mode
    * @exception IllegalArgumentException
    *               if the value of the horizontal gap is less than zero.
    */
   public TrainLayout(Container self, int hgap, String testname)
   {
      this(self, hgap, testname, LayoutMode.TEST_TRAIN);
   }
   
   /**
    * private constructor  
    * <p>
    * 
    * All <code>TrainLayout</code> constructors defer to this one.
    */
   private TrainLayout(Container self, int hgap, String testname, LayoutMode mode)
   {
      if (hgap < 0)
         throw new IllegalArgumentException(
               "the horizontal gap can not be a negativ number");
      this.hgap = hgap;
      this.dimMin = null;
      this.dimMax = null;
      this.self = self;
      this.testname = testname;
      this.mode = mode;
   }

   /**
    * Gets the horizontal gap between components.
    * 
    * @return the horizontal gap between components
    * @since private
    */
   public int getHgap()
   {
      return hgap;
   }

   /**
    * Sets the horizontal gap between components to the specified value.
    * 
    * @param hgap
    *           the horizontal gap between components
    * @since private
    */
   public void setHgap(int hgap)
   {
      if (hgap < 0)
         throw new IllegalArgumentException(
               "the horizontal gap can not be a negativ number");
      this.hgap = hgap;
   }

   /**
    * Determines the preferred size of the container argument using this train
    * layout.
    * <p>
    * The preferred size is all size available
    *
    * @param self
    *           the container in which to do the layout
    * @return the preferred dimensions to lay out the subcomponents of the
    *         specified container
    * @see java.awt.Container#getPreferredSize()
    */
   public Dimension preferredLayoutSize(Container self)
   {
      synchronized (self.getTreeLock())
      {
         checkContainer(self);
         int ncomponents = self.getComponentCount();
         if (ncomponents == 0)
         {
            if (self.getMinimumSize() != null)
            {
               return self.getMinimumSize();
            }
            else if (self.getParent() instanceof JViewport)
            {
               JViewport vp = (JViewport) self.getParent();
               Insets insets = self.getInsets();
               return new Dimension(vp.getWidth() - insets.left - insets.right,
                     vp.getHeight() - insets.top - insets.bottom);
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

         h = self.getHeight() - (insets.top + insets.bottom);
         w = self.getWidth() - (insets.left + insets.right)
               - hgap * (ncomponents - 1);

         int hmin = 0;
         int hmax = Integer.MAX_VALUE;
         int wmintotal = 0;
         int[] wmin = new int[ncomponents];
         int[] wmax = new int[ncomponents];
         for (int i = 0; i < ncomponents; i++)
         {
            Component comp = self.getComponent(i);
            Dimension dmin;
            Dimension dmax;
            /*
             * In case Component is Container with Layout instance of
             * TrainLayout or TotemLayout the dimensions derived by content - if
             * any - should override given Dimensions. Only when there is no
             * content the given Dimensions should be used.
             */
            if (comp instanceof Container && (((Container) comp)
                  .getLayout() instanceof TotemLayout
                  || ((Container) comp).getLayout() instanceof TrainLayout
                  || ((Container) comp).getLayout() instanceof BullsEyeLayout))
            {
               Dimension dminContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).minimumLayoutSize((Container) comp);
               if (dminContent != null)
                  dmin = dminContent;
               else
                  dmin = comp.getMinimumSize();
               Dimension dmaxContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).maximumLayoutSize((Container) comp);
               if (dmaxContent != null)
                  dmax = dmaxContent;
               else
                  dmax = comp.getMaximumSize();
            }
            else
            {
               dmin = comp.getMinimumSize();
               dmax = comp.getMaximumSize();
            }

            // MINIMUM
            if (dmin != null)
            {
               if (dmin.height > hmin)
                  hmin = dmin.height; // minheight is maximized
               wmin[i] = dmin.width;
               wmintotal += dmin.width;
            }
            else // minimum was not set on innermost layer
            {
               wmin[i] = w / ncomponents;
               wmintotal += w / ncomponents;
            }
            // MAXIMUM
            if (dmax != null)
            {

               if (dmax.height < hmax)
                  hmax = dmax.height; // maxheight is minimized
               wmax[i] = dmax.width;
            }
            else // maximum was not set on innermost layer
            {
               wmax[i] = w / ncomponents;
            }
         }

         // height
         if (hmin > hmax)
         {
            // error correction, to show error use TrainLayoutTest or toString()
            hmax = hmin;
         }
         
         if (hmax != Integer.MAX_VALUE)
         {
            if (h <= hmin)
               h = hmin;
            else if (hmax < h)
               h = hmax;
            // else h = h;
         }
         else if (h < hmin)
            h = hmin;
         // else h = h;
         //
         // width
         int[] wfinal = new int[ncomponents];
         int wcompare = 0;
         int[] wdifference = new int[ncomponents];
         int wdifferencetotal = 0;
         for (int i = 0; i < ncomponents; i++)
         {
            if (wmax[i] < wmin[i])
            {
               // error correction, to show error use TrainLayoutTest or
               // toString
               wmax[i] = wmin[i];
            }
            // allocating available width according to minimum widths vs.
            // wmintotal
            wfinal[i] = (int) ((wmin[i] / (float) wmintotal) * w);
            if (wmin[i] > wfinal[i])
            {
               wfinal[i] = wmin[i];
            }
            else if (wmax[i] < wfinal[i])
            {
               wfinal[i] = wmax[i];
            }
            wcompare += wfinal[i];
            wdifference[i] = wmax[i] - wfinal[i];
            wdifferencetotal += wdifference[i];
         }
         int wleftover = w - wcompare;
         // dispensing possible wleftover according to wdifference vs.
         // wdifferencetotal
         if (wleftover > 0)
         {
            wcompare = 0;
            for (int i = 0; i < ncomponents; i++)
            {
               wfinal[i] += (int) ((wdifference[i] / (float) wdifferencetotal)
                     * wleftover);
               if (wmax[i] < wfinal[i])
               {
                  wfinal[i] = wmax[i];
               }
               wcompare += wfinal[i];
            }
         }
         wleftover = w - wcompare;
         // dispensing possible wleftover from back to front
         if (wleftover > 0)
         {
            for (int i = ncomponents - 1; i >= 0; i--)
            {
               int wdiff = wmax[i] - wfinal[i];
               if (wdiff > 0 && wdiff < wleftover)
               {
                  wfinal[i] = wmax[i];
                  wleftover -= wdiff;
               }
               else if (wdiff > 0)
               {
                  wfinal[i] += wleftover;
                  break;
               }
            }
         }

         int wfinaltotal = insets.left;
         for (int i = 0; i < ncomponents; i++)
         {
            wfinaltotal += wfinal[i] + hgap;
         }

         return new Dimension(wfinaltotal, h);
      }
   }

   /**
    * Determines the minimum size of the container argument using this train
    * layout.
    * <p>
    * The minimum height of a train layout is the largest minimum height of all
    * of the components in the container, plus the top and bottom insets of the
    * self container.
    * <p>
    * The minimum width of a train layout is the sum of minimum widths of all of
    * the components in the container, plus the horizontal padding times the
    * number of items minus one, plus the left and right insets of the self
    * container.
    *
    * @param self
    *           the container in which to do the layout
    * @return the minimum dimensions needed to lay out the subcomponents of the
    *         specified container
    * @see java.awt.Container#doLayout
    */
   public Dimension minimumLayoutSize(Container self)
   {
      synchronized (self.getTreeLock())
      {
         checkContainer(self);
         if (dimMin != null)
            return dimMin;
         int ncomponents = self.getComponentCount();
         if (ncomponents == 0)
         {
            dimMin = null;
            return null;
         }

         Insets insets = self.getInsets();
         int h = 0;
         int w = 0;
         for (int i = 0; i < ncomponents; i++)
         {
            Component comp = self.getComponent(i);
            Dimension dmin;
            /*
             * In case Component is Container with Layout instance of
             * TrainLayout or TotemLayout the dimensions derived by content - if
             * any - should override given Dimensions. Only when there is no
             * content the given Dimensions should be used.
             */
            if (comp instanceof Container && (((Container) comp)
                  .getLayout() instanceof TotemLayout
                  || ((Container) comp).getLayout() instanceof TrainLayout
                  || ((Container) comp).getLayout() instanceof BullsEyeLayout))
            {
               Dimension dminContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).minimumLayoutSize((Container) comp);
               if (dminContent != null)
                  dmin = dminContent;
               else
                  dmin = comp.getMinimumSize();
            }
            else
            {
               dmin = comp.getMinimumSize();
            }
            
            if (dmin != null)
            {
               if (h < dmin.height)
                  h = dmin.height; // minheight is maximized
               w += dmin.width;
            }
            else
            {
               w += (self.getWidth() - (insets.left + insets.right))
                     / ncomponents;
            }
         }

         dimMin = new Dimension(
               insets.left + insets.right + w + (ncomponents - 1) * hgap,
               insets.top + insets.bottom + h);
         return dimMin;

      }
   }

   /**
    * Determines the maximum size of the container argument using this train
    * layout.
    * <p>
    * The maximum height of a train layout is the smallest maximum height of all
    * of the components in the container, plus the top and bottom insets of the
    * self container.
    * <p>
    * The maximum width of a train layout is the sum of maximum widths of all of
    * the components in the container, plus the horizontal padding times the
    * number of items minus one, plus the left and right insets of the self
    * container.
    *
    * @param self
    *           the container in which to do the layout
    * @return the maximum dimensions needed to lay out the subcomponents of the
    *         specified container
    * @see java.awt.Container#doLayout
    */
   @Override
   public Dimension maximumLayoutSize(Container self)
   {
      synchronized (self.getTreeLock())
      {
         checkContainer(self);
         if (dimMax != null)
            return dimMax;
         int ncomponents = self.getComponentCount();
         if (ncomponents == 0)
         {
            dimMax = null;
            return null;
         }

         Insets insets = self.getInsets();
         int h = Integer.MAX_VALUE;
         int w = 0;
         for (int i = 0; i < ncomponents; i++)
         {
            Component comp = self.getComponent(i);
            Dimension dmax;
            /*
             * In case Component is Container with Layout instance of
             * TrainLayout or TotemLayout the dimensions derived by content - if
             * any - should override given Dimensions. Only when there is no
             * content the given Dimensions should be used.
             */
            if (comp instanceof Container && (((Container) comp)
                  .getLayout() instanceof TotemLayout
                  || ((Container) comp).getLayout() instanceof TrainLayout
                  || ((Container) comp).getLayout() instanceof BullsEyeLayout))
            {
               Dimension dmaxContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).maximumLayoutSize((Container) comp);
               if (dmaxContent != null)
                  dmax = dmaxContent;
               else
                  dmax = comp.getMaximumSize();
            }
            else
            {
               dmax = comp.getMaximumSize();
            }
            if (dmax != null)
            {
               if (h > dmax.height)
                  h = dmax.height; // maxheight is minimized
               w += dmax.width;
            }
            else
            {
               w += (self.getWidth() - (insets.left + insets.right))
                     / ncomponents;
            }
         }
         dimMax = new Dimension(
               insets.left + insets.right + w + (ncomponents - 1) * hgap,
               insets.top + insets.bottom + h);
         return dimMax;
      }
   }

   /**
    * Lays out the specified container using this layout.
    * <p>
    * This method reshapes the components in the specified self container in
    * order to satisfy the constraints of the <code>TrainLayout</code> object.
    * <p>
    * All components in a train layout are given the same height, which is equal
    * to the available space minus parent insets. If the smallest maximum height
    * is less, than this overrides the height. Else if the largest minimum
    * height is more, than this overrides the height.
    * <p>
    * The train layout manager divides the available width according to the
    * relative sizes of the minimum widths if available, empty minimum widths
    * are substituted by the preferred width if available, otherwise the 1/nth
    * width is used for relative sizing. The minimum and maximum widths of each
    * item are observed.
    *
    * @param self
    *           the container in which to do the layout
    * @see java.awt.Container
    * @see java.awt.Container#doLayout
    */
   public void layoutContainer(Container self)
   {
      synchronized (self.getTreeLock())
      {
         checkContainer(self);
         int ncomponents = self.getComponentCount();
         if (ncomponents == 0)
            return;

         Insets insets = self.getInsets();
         int availableHeight;
         int availableWidth;
         if (self.getParent() instanceof JViewport)
         {
            JViewport vp = (JViewport) self.getParent();
            availableHeight = vp.getHeight() - (insets.top + insets.bottom);
            availableWidth = vp.getWidth() - (insets.left + insets.right)
                  - hgap * (ncomponents - 1);
         }
         else
         {
            availableHeight = self.getHeight() - (insets.top + insets.bottom);
            availableWidth = self.getWidth() - (insets.left + insets.right)
                  - hgap * (ncomponents - 1);
         }
         
         int h = availableHeight;
         int w = availableWidth;
         int hmin = 0;
         int hmax = Integer.MAX_VALUE;
         int wmintotal = 0;
         int[] wmin = new int[ncomponents];
         int[] wmax = new int[ncomponents];
         for (int i = 0; i < ncomponents; i++)
         {
            Component comp = self.getComponent(i);
            Dimension dmin;
            Dimension dmax;
            /*
             * In case Component is Container with Layout instance of
             * TrainLayout or TotemLayout the dimensions derived by content - if
             * any - should override given Dimensions. Only when there is no
             * content the given Dimensions should be used.
             */
            if (comp instanceof Container && (((Container) comp)
                  .getLayout() instanceof TotemLayout
                  || ((Container) comp).getLayout() instanceof TrainLayout
                  || ((Container) comp).getLayout() instanceof BullsEyeLayout))
            {
               Dimension dminContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).minimumLayoutSize((Container) comp);
               if (dminContent != null)
                  dmin = dminContent;
               else
                  dmin = comp.getMinimumSize();
               Dimension dmaxContent = ((LayoutManager2) ((Container) comp)
                     .getLayout()).maximumLayoutSize((Container) comp);
               if (dmaxContent != null)
                  dmax = dmaxContent;
               else
                  dmax = comp.getMaximumSize();
            }
            else
            {
               dmin = comp.getMinimumSize();
               dmax = comp.getMaximumSize();
            }

            // MINIMUM
            if (dmin != null)
            {
               if (dmin.height > hmin)
                  hmin = dmin.height; // minheight is maximized
               wmin[i] = dmin.width;
               wmintotal += dmin.width;
            }
            else // minimum was not set on innermost layer
            {
               wmin[i] = w / ncomponents;
               wmintotal += w / ncomponents;
            }
            // MAXIMUM
            if (dmax != null)
            {
               if (dmax.height < hmax)
                  hmax = dmax.height; // maxheight is minimized
               wmax[i] = dmax.width;
            }
            else // maximum was not set on innermost layer
            {
               wmax[i] = w / ncomponents;
            }
         }

         // height
         if (hmin > hmax)
         {
            // error correction, to show error use TrainLayoutTest or toString()
            hmax = hmin;
         }
         
         if (hmax != Integer.MAX_VALUE)
         {
            if (h <= hmin)
               h = hmin;
            else if (hmax < h)
               h = hmax;
            // else h = h;
         }
         else if (h < hmin)
            h = hmin;
         // else h = h;
         
         // width
         int[] wfinal = new int[ncomponents];
         int wcompare = 0;
         int[] wdifference = new int[ncomponents];
         int wdifferencetotal = 0;
         for (int i = 0; i < ncomponents; i++)
         {
            if (wmax[i] < wmin[i])
            {
               // error correction, to show error use TrainLayoutTest or
               // toString()
               wmax[i] = wmin[i];
            }
            // allocating available width according to minimum widths vs.
            // wmintotal
            wfinal[i] = (int) ((wmin[i] / (float) wmintotal) * w);
            if (wmin[i] > wfinal[i])
            {
               wfinal[i] = wmin[i];
            }
            else if (wmax[i] < wfinal[i])
            {
               wfinal[i] = wmax[i];
            }
            wcompare += wfinal[i];
            wdifference[i] = wmax[i] - wfinal[i];
            wdifferencetotal += wdifference[i];
         }
         int wleftover = w - wcompare;
         // dispensing possible wleftover according to wdifference vs.
         // wdifferencetotal
         if (wleftover > 0)
         {
            wcompare = 0;
            for (int i = 0; i < ncomponents; i++)
            {
               wfinal[i] += (int) ((wdifference[i] / (float) wdifferencetotal)
                     * wleftover);
               if (wmax[i] < wfinal[i])
               {
                  wfinal[i] = wmax[i];
               }
               wcompare += wfinal[i];
            }
         }
         wleftover = w - wcompare;
         // dispensing possible wleftover from back to front
         if (wleftover > 0)
         {
            for (int i = ncomponents - 1; i >= 0; i--)
            {
               int wdiff = wmax[i] - wfinal[i];
               if (wdiff > 0 && wdiff < wleftover)
               {
                  wfinal[i] = wmax[i];
                  wleftover -= wdiff;
               }
               else if (wdiff > 0)
               {
                  wfinal[i] += wleftover;
                  break;
               }
            }
         }

         int x = insets.left;
         for (int i = 0; i < ncomponents; i++)
         {
            Component comp = self.getComponent(i);
            comp.setBounds(x, insets.top, wfinal[i], h);
            x += wfinal[i] + hgap;
         }
         
         if (LayoutMode.TEST_TRAIN == this.mode)
         {
            System.out.println("");
            System.out.println(testname + " with TrainLayout");
            System.out.println("available width: " + availableWidth);
            System.out.println("available height: " + availableHeight);
            System.out.println("all components height: " + h);
            System.out.println("all components min height: " + hmin);
            System.out.println("all components max height: " + hmax);
            for (int i = 0; i < ncomponents; i++)
            {               
               System.out.println("component["+i+"] width: "+ wfinal[i]);
            }
            System.out.println("");           
         }        
      }
   }

   /**
    * invalidates Layout, minimum and maximum sizes of content will be
    * recalculated
    * 
    * @param name
    *           the name of the component
    * @param comp
    *           the component to be added
    */
   @Override
   public void addLayoutComponent(String name, Component comp)
   {
      invalidateLayout(comp.getParent());
   }

   /**
    * invalidates Layout, minimum and maximum sizes of content will be
    * recalculated
    *
    * @param name
    *           the name of the component
    * @param comp
    *           the component to be added
    */
   @Override
   public void addLayoutComponent(Component comp, Object constraints)
   {
      invalidateLayout(comp.getParent());
   }

   /**
    * invalidates Layout, minimum and maximum sizes of content will be
    * recalculated
    * 
    * @param comp
    *           the component to be removed
    */
   @Override
   public void removeLayoutComponent(Component comp)
   {
      invalidateLayout(comp.getParent());
   }

   /**
    * Invalidates the layout, indicating that if the layout manager has cached
    * information it should be discarded.
    */
   @Override
   public void invalidateLayout(Container self)
   {
      checkContainer(self);
      this.dimMin = null;
      this.dimMax = null;
      if (self.getParent() != null && self.getParent().getLayout() != null
            && (self.getParent().getLayout() instanceof TotemLayout
                  || self.getParent().getLayout() instanceof TrainLayout))
      {
         ((LayoutManager2) self.getParent().getLayout())
               .invalidateLayout(self.getParent());
      }
   }

   /**
    * Returns the alignment along the x axis. This specifies how the component
    * would like to be aligned relative to other components. The value should be
    * a number between 0 and 1 where 0 represents alignment along the origin, 1
    * is aligned the furthest away from the origin, 0.5 is centered, etc.
    */
   @Override
   public float getLayoutAlignmentX(Container self)
   {
      return 0;
   }

   /**
    * Returns the alignment along the y axis. This specifies how the component
    * would like to be aligned relative to other components. The value should be
    * a number between 0 and 1 where 0 represents alignment along the origin, 1
    * is aligned the furthest away from the origin, 0.5 is centered, etc.
    */
   @Override
   public float getLayoutAlignmentY(Container self)
   {
      return 0;
   }

   private void checkContainer(Container self)
   {
      if (this.self != self)
      {
         throw new AWTError("TrainLayout can't be shared");
      }
   }
}
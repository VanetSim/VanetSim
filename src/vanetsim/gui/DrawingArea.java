/*
 * VANETsim open source project - http://www.vanet-simulator.org
 * Copyright (C) 2008 - 2013  Andreas Tomandl, Florian Scheuer, Bernhard Gruber
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vanetsim.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.RepaintManager;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.gui.controlpanels.ResearchSeriesDialog;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.scenario.Scenario;

/**
 * This class represents a <code>JComponent</code> on which all map elements are painted. It just creates
 * the basic system which is needed, rendering itself is delegated to the {@link Renderer}-class!
 * 
 * @see vanetsim.gui.Renderer
 */
public final class DrawingArea extends JComponent implements MouseWheelListener, KeyListener, MouseListener{

	/** The constant for serializing. */
	private static final long serialVersionUID = -5210801710805449919L;
	
	/** The zoom value. Should be higher than 1. Setting this lower means faster zooming with mouse wheel, setting it higher means slower zooming. */
	private static final double ZOOM_VALUE = 5.0;

	/** A reference to the singleton instance of the {@link Renderer} because we need this quite often and don't want to rely on compiler inlining. */
	private final Renderer renderer_ = Renderer.getInstance();
	
	/** If <code>true</code>, a temporary Image is used to create a manual DoubleBuffering. */
	private final boolean drawManualBuffered_;
	
	/** An <code>AffineTransform</code> which does not transform any coordinates. */
	private final AffineTransform nullTransform_ = new AffineTransform();
	
	/** The street map is static (only changed through zooming/panning) so it's efficient to just store its image representation in memory. */
	private BufferedImage streetsImage_ = null;

	/** A temporary <code>BufferedImage</code> which is used when <code>DrawManualBuffered=true</code>. */
	private BufferedImage temporaryImage_ = null;
	
	/** An image in which the current scale is drawn. */
	private BufferedImage scaleImage_ = null;

	/** A temporary <code>Graphics2D</code> based on <code>temporaryImage</code>. */
	private Graphics2D temporaryG2d_ = null;

	/**
	 * Constructor.
	 * 
	 * @param useDoubleBuffer		<code>true</code> to set DoubleBuffering on, <code>false</code> to set it off
	 * @param drawManualBuffered	set to <code>true</code> to use a <code>BufferdImage</code> for drawing (manual DoubleBuffering)
	 */
	public DrawingArea(boolean useDoubleBuffer, boolean drawManualBuffered){
		drawManualBuffered_ = drawManualBuffered;
		setBackground(Color.white);
		setDoubleBuffered(useDoubleBuffer);
		setOpaque(true);
		setIgnoreRepaint(false);
		setFocusable(true);
		addMouseWheelListener(this);
		addKeyListener(this);
		addMouseListener(this);
		ErrorLog.log(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getAvailableAcceleratedMemory()/(1024*1024) + Messages.getString("DrawingArea.acceleratedVRAM"), 4, this.getName(), "init", null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This method gets automatically called on a <code>repaint()</code>. Therefore, rendering is delegated from here to
	 * the renderer.
	 * 
	 * @param g	the <code>Graphics</code> object to paint on
	 */
	public void paintComponent(Graphics g){
		// Note: Normally, "super.paintComponent(_g)" should be called here to prevent garbage on the screen.
		// However, this can induce a huge performance hit and as the buffered streetimage is as large as the area,
		// it shouldn't give problems with garbage shining through.
		//to prevent this function from overwriting anything while rendering is in progress! 
		synchronized(renderer_){
			if(streetsImage_ == null || getWidth() != streetsImage_.getWidth() || getHeight() != streetsImage_.getHeight()){
				prepareBufferedImages();
			}
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	
			if(drawManualBuffered_ == true){
				temporaryG2d_.setTransform(nullTransform_);
				temporaryG2d_.drawImage(streetsImage_, 0, 0, null, this);	// draw the prerendered static objects
				renderer_.drawMovingObjects(temporaryG2d_);	// draw moving objects
				temporaryG2d_.drawImage(scaleImage_, getWidth()-120, getHeight()-40, null, this);		//draw the measure
				g2d.drawImage(temporaryImage_, 0, 0, null, this);	// output temporary image
			} else {
				g2d.drawImage(streetsImage_, 0, 0, null, this); // draw the prerendered static objects
				renderer_.drawMovingObjects(g2d);	// draw moving objects
				g2d.drawImage(scaleImage_, getWidth()-120, getHeight()-40, null, this);		//draw the measure
			}
	
			g2d.dispose();	// should be disposed to aid garbage collector
		}
	}
	
	/**
	 * Prepares all <code>BufferedImages</code> and notifies the {@link Renderer} of a new drawing area size.
	 */
	public void prepareBufferedImages(){
		if(streetsImage_ == null || getWidth() != streetsImage_.getWidth() || getHeight() != streetsImage_.getHeight()){	//prepare new image for streets ("static objects")
			renderer_.setDrawHeight(getHeight());
			renderer_.setDrawWidth(getWidth());
			renderer_.updateParams();
			streetsImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
		}
		if(drawManualBuffered_ == true && (temporaryImage_ == null || getWidth() != temporaryImage_.getWidth() || getHeight() != temporaryImage_.getHeight())){	//create image for manual double buffering
			temporaryImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
			temporaryG2d_ = temporaryImage_.createGraphics();
			temporaryG2d_.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		if(scaleImage_ == null){		//just needs to be created one single time!
			scaleImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(100, 30, Transparency.OPAQUE);
			Graphics2D tmpgraphics = scaleImage_.createGraphics();
			tmpgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			tmpgraphics.setColor(Color.black);
			tmpgraphics.fillRect(0, 0, 100, 30);
		}
		renderer_.drawStaticObjects(streetsImage_);
		renderer_.drawScale(scaleImage_);
	}

	/**
	 * This function gets called when a property value changes such that size, location or internal layout of
	 * change. We then need to check if our <code>BufferdImages</code> still have the correct size and create new ones if needed!
	 */
	public void revalidate(){
		super.revalidate();
		if(this.getWidth() > 0 && this.getHeight() > 0 && (this.getWidth() != streetsImage_.getWidth() || this.getHeight() != streetsImage_.getHeight())){
			this.prepareBufferedImages();
		}
	}

	/**
	 * Setting the <code>RepaintManager</code> like seen
	 * <a href=http://java.sun.com/products/java-media/2D/samples/index.html>on the official examples for Java2D</a>
	 * (link last time checked on 12.08.2008).<br>
	 * This imitates the "On Screen" method used there and in some cases drastically improves performance (even when
	 * DoubleBuffering of this <code>JComponent</code> is off the DoubleBuffering might still be on because the
	 * DoubleBuffering is inherited from the main <code>JFrame</code>!).
	 * 
	 * @param x			the x coordinate for the bounding box to repaint
	 * @param y			the y coordinate for the bounding box to repaint
	 * @param width		the width
	 * @param height	the height
	 */
	public void paintImmediately(int x, int y, int width, int height){
		RepaintManager repaintManager = null;
		boolean save = true;
		if (!isDoubleBuffered()) {
			repaintManager = RepaintManager.currentManager(this);
			save = repaintManager.isDoubleBufferingEnabled();
			repaintManager.setDoubleBufferingEnabled(false);
		}
		super.paintImmediately(x, y, width, height);

		if (repaintManager != null) repaintManager.setDoubleBufferingEnabled(save);
	}

	/**
	 * Listener for mouse scrolls.
	 * 
	 * @param e	the <code>MouseWheelEvent</code>
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e){
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL && e.getUnitsToScroll() != 0){
			int scrollValue = e.getUnitsToScroll();
			double newzoom = renderer_.getMapZoom();
			if(scrollValue > 0){
				for(int i= 0; i < scrollValue; i+=3){
					newzoom -= newzoom/ZOOM_VALUE;				
				}
			} else {
				for(int i= 0; i > scrollValue; i-=3){
					newzoom += newzoom/ZOOM_VALUE;				
				}
			}
			renderer_.setMapZoom(newzoom);
			VanetSimStart.getMainControlPanel().getSimulatePanel().setZoomValue((int)Math.round(Math.log(renderer_.getMapZoom()*1000)*50));
			ReRenderManager.getInstance().doReRender();
		}
	}


	/**
	 * Does nothing. Just necessary to implement the <code>KeyListener</code>.
	 * 
	 * @param e	the <code>KeyEvent</code>
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e){
	}


	/**
	 * Allows panning through pressing the keyboard arrows.
	 * 
	 * @param e	the <code>KeyEvent</code>
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e){
		int keycode = e.getKeyCode();

		if(keycode == 38){
			renderer_.pan('u');
			ReRenderManager.getInstance().doReRender();
		} else if (keycode == 40){
			renderer_.pan('d');
			ReRenderManager.getInstance().doReRender();
		} else if (keycode == 37){
			renderer_.pan('l');
			ReRenderManager.getInstance().doReRender();
		} else if (keycode == 39){
			renderer_.pan('r');
			ReRenderManager.getInstance().doReRender();
		}
		else if(keycode == KeyEvent.VK_SPACE){
			VanetSimStart.getMainControlPanel().getSimulatePanel().toggleSimulationStatus();
		}
		else if(keycode == KeyEvent.VK_H){
			VanetSimStart.getMainControlPanel().tooglePanel();
		}
		else if(keycode == KeyEvent.VK_M){
			VanetSimStart.getMainControlPanel().getSimulatePanel().toggleMixZones();
		}
		else if(keycode == KeyEvent.VK_I){
			VanetSimStart.getMainControlPanel().getSimulatePanel().toggleVehileIDs();
		}
		else if(keycode == KeyEvent.VK_C){
			VanetSimStart.getMainControlPanel().getSimulatePanel().toggleCommunicationDistance();
		}
		else if(keycode == KeyEvent.VK_K){
			VanetSimStart.getMainControlPanel().getSimulatePanel().toggleVehicleConnections();
		}
		else if(keycode == KeyEvent.VK_R){
			toggleResearchCreator();
		}
		else if(keycode == KeyEvent.VK_1){
			VanetSimStart.getMainControlPanel().switchToTab(0);
		}
		else if(keycode == KeyEvent.VK_2){
			VanetSimStart.getMainControlPanel().switchToTab(1);
		}
		else if(keycode == KeyEvent.VK_3){
			VanetSimStart.getMainControlPanel().switchToTab(2);
		}
		else if(keycode == KeyEvent.VK_4){
			VanetSimStart.getMainControlPanel().switchToTab(3);
		}
		else if(keycode == KeyEvent.VK_S){
			VanetSimStart.getMainControlPanel().changeFileChooser(true, true, false);
			int returnVal = VanetSimStart.getMainControlPanel().getFileChooser().showOpenDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) { 
				Runnable job = new Runnable() {
					public void run() {
						Scenario.getInstance().load(VanetSimStart.getMainControlPanel().getFileChooser().getSelectedFile(), false);
					}
				};
				new Thread(job).start();
			}
		}
		else if(keycode == KeyEvent.VK_O){
			VanetSimStart.getMainControlPanel().changeFileChooser(true, true, false);
			int returnVal = VanetSimStart.getMainControlPanel().getFileChooser().showOpenDialog(VanetSimStart.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {           
				Runnable job = new Runnable() {
					public void run() {
						Map.getInstance().load(VanetSimStart.getMainControlPanel().getFileChooser().getSelectedFile(), false);
					}
				};
				new Thread(job).start();
			}
		}
	}

	/**
	 * Tracks clicks in order to get focus and allow to get information about points on the map or edit something on the map.
	 * The work itself is done in the <code>MousedragManager</code>.
	 * 
	 * @param e	the <code>MouseEvent</code>
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON1){
			requestFocusInWindow();
			MouseClickManager.getInstance().signalPressed(e.getX(), e.getY());	    	
		}
	}

	/**
	 * Used for panning through mousedragging through the <code>MousedragManager</code>.
	 * 
	 * @param e	the <code>MouseEvent</code>
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON1){
			MouseClickManager.getInstance().signalReleased(e.getX(), e.getY());
		}
	}

	/**
	 * Does nothing. Just necessary to implement the <code>KeyListener</code>.
	 * 
	 * @param e	the <code>KeyEvent</code>
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e){
	}

	/**
	 * Does nothing. Just necessary to implement the <code>MouseListener</code>.
	 * 
	 * @param e	the <code>MouseEvent</code>
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e){
	}

	/**
	 * Notifies the <code>MouseDragManager</code> that mouse entered this area.
	 * 
	 * @param e	the <code>MouseEvent</code>
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e){
		requestFocusInWindow();
		MouseClickManager.getInstance().setActive(true);
	}

	/**
	 * Notifies the <code>MouseDragManager</code> that mouse left this area.
	 * 
	 * @param e	the <code>MouseEvent</code>
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e){
		MouseClickManager.getInstance().setActive(false);
	}
	
	/**
	 * toggle research creator visibility
	 */
	public void toggleResearchCreator(){
		ResearchSeriesDialog.getInstance().setVisible(true);
	}
}

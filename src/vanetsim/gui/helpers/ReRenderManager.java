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
package vanetsim.gui.helpers;

import vanetsim.gui.Renderer;

/**
 * A small manager to get better user experience through buffering ReRender-calls.
 * By using this, the GUI gets (almost) immediately responsive again upon clicking a button. If rendering the background takes 
 * longer than the time between multiple button clicks (made by the user), some ReRender-events are kind of silently dropped.
 */
public final class ReRenderManager extends Thread{
	
	/** The only instance of this class (singleton). */
	private static final ReRenderManager INSTANCE = new ReRenderManager();
	
	/** A variable indicating if a ReRender is scheduled. */
	private boolean doRender_ = false;
	
	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private ReRenderManager(){
	}	
	
 	/**
	  * Gets the single instance of this manager.
	  * 
	  * @return single instance of this manager
	  */
	 public static ReRenderManager getInstance(){
 		return INSTANCE;
	}
	
	/**
	 * Schedule a re-render-Operation.
	 */
	public void doReRender(){
		doRender_ = true;
	}
	
	/** 
	 * A thread which checks if a re-rendering is scheduled and then sleeps 50ms. If necessary, it
	 * calls the {@link Renderer} to perform a full re-render.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		setName("ReRenderManager"); //$NON-NLS-1$
		setPriority(Thread.MIN_PRIORITY);
		Renderer renderer = Renderer.getInstance();
		while(true){
			if(doRender_){
				doRender_ = false;
				renderer.ReRender(true, false);
			}
			try{
				Thread.sleep(10);
			} catch (Exception e){};
		}
	}
}
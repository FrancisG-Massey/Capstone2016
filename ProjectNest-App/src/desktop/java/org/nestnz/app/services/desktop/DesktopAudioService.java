/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
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
 *******************************************************************************/
package org.nestnz.app.services.desktop;

import org.nestnz.app.services.AudioService;

import javafx.scene.media.AudioClip;

/**
 * 
 */
public class DesktopAudioService implements AudioService {
	
	private AudioClip clip;

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#play(java.lang.String)
	 */
	@Override
	public void play(String audioName, double volume) {
		if (clip != null) {
			clip.stop();
		}
		clip = new AudioClip(audioName);
		clip.play(volume);
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#stop()
	 */
	@Override
	public void stop() {
		clip.stop();
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#pause()
	 */
	@Override
	public void pause() {
		//Not supported on Desktop
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#resume()
	 */
	@Override
	public void resume() {
		//Not supported on Desktop
	}

}

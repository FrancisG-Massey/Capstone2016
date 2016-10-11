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
package org.nestnz.app.services.ios;

import org.nestnz.app.services.AudioService;

public class IOSAudioService implements AudioService {
	
	static {
        System.loadLibrary("Audio");
    }

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#play(java.lang.String, double)
	 */
	@Override
	public void play(String audioName, double volume) {
		audioPlay(audioName, volume);
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#stop()
	 */
	@Override
	public void stop() {
		audioStop();
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#pause()
	 */
	@Override
	public void pause() {
		audioPause();
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#resume()
	 */
	@Override
	public void resume() {
		audioResume();
	}

	private native void audioPlay(String audioName, double volume);
	
	private native void audioStop();
	
	private native void audioPause();
	
	private native void audioResume();
}

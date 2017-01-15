/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.dialogs;

import midi.pad.ui.widgets.NoButton;
import midi.pad.ui.widgets.YesButton;

/**
 * @author oliver
 *
 */
public class ConfirmDialog extends HintDialog {

	public ConfirmDialog(final String question, final Runnable confirmRunner,
			final Runnable regretRunner) {
		super(question);
		setWidgets(new YesButton(4, 4, confirmRunner), new NoButton(0, 4, regretRunner));
		start();
	}
}

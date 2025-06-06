/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

package space.arim.dazzleconf.backend.yaml.samples.ness;

import space.arim.dazzleconf2.engine.Comments;
import space.arim.dazzleconf2.engine.liaison.SubSection;

import java.util.Set;

@Comments({
		"",
		"AutoClick",
		"Caps clicks per second (CPS) at a hard limit, also calculates",
		"the variance in the user's clicks (deviation) and the variance",
		"in the variance (super deviation).",
		"",
		"Performance impact: Minimal",
		""})
public interface AutoClickConfig {

	@Comments("Clicks older than this are completely ignored")
	default int totalRetentionSecs() {
		return 32;
	}

	@SubSection HardLimit hardLimit();

	interface HardLimit {

		@Comments({
				"These are pairs of CPS limits and required time spans",
				"",
				"If the player's CPS measured over the time span is greater than the CPS limit, a violation is triggered.",
				"",
				"      # For example, '16:3' means that if the player's clicks in the past 3 seconds average 16 CPS, ",
				"trigger a violation."})
		default Set<HardLimitEntry> cpsAndRequiredSpan() {
			return Set.of(new HardLimitEntry(35, 2));
		}
	}
	@Comments("# A more advanced consistency check")
	@SubSection Constancy constancy();

	interface Constancy {

		@Comments({
				"These are pairs of standard deviation percentages and sample counts",
				"",
				"The first number is a deviation percentage, and the second number is the sample count.",
				"",
				"The standard deviation is calculated based on the interval between clicks in the sample.",
				"Then, the standard deviation percentage is calculated as the standard deviation as percent of the average.",
				"",
				"If there are enough samples, and the deviation percent is less than the required deviation,",
				"a violation is triggered.",
				"",
				"For example, '30:8' means that if the standard deviation in the intervals between clicks in a sample",
				"divided by the average interval, is less than 30%, trigger a violation if the sample size is at least 8."
		})
		default Set<DeviationEntry> deviationAndSample() {
			return Set.of(new DeviationEntry(30, 10));
		}

		@Comments({
				"These are pairs of standard deviation percentages and sample counts",
				"",
				"These are conceptually similar to the previous. However, this measures the standard deviations between",
				"the standard deviations. Thus, it is called the \"super deviation\"."
		})
		default Set<DeviationEntry> superdeviationAndSupersample() {
			return Set.of(new DeviationEntry(60, 10));
		}

	}

}

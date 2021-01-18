/*
 * DazzleConf
 * Copyright © 2020 Anand Beh
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

package space.arim.dazzleconf.ext.snakeyaml.ness;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.ConfSerialisers;
import space.arim.dazzleconf.annote.SubSection;

import java.util.Set;

@ConfSerialisers(HardLimitSerialiser.class)
@ConfHeader({
		"",
		"AutoClick",
		"Caps clicks per second (CPS) at a hard limit, also calculates",
		"the variance in the user's clicks (deviation) and the variance",
		"in the variance (super deviation).",
		"",
		"Performance impact: Minimal",
		""})
public interface AutoClickConfig {

	@ConfKey("total-retention-secs")
	@ConfComments("Clicks older than this are completely ignored")
	@ConfDefault.DefaultInteger(32)
	int totalRetentionSecs();

	@ConfKey("hard-limit.cps-and-required-span")
	@ConfComments({
			"These are pairs of CPS limits and required time spans",
			"",
			"If the player's CPS measured over the time span is greater than the CPS limit, a violation is triggered.",
			"",
			"      # For example, '16:3' means that if the player's clicks in the past 3 seconds average 16 CPS, ",
			"trigger a violation."})
	@ConfDefault.DefaultStrings({"35:2"})
	Set<HardLimitEntry> hardLimits();

	@SubSection
	@ConfComments("# A more advanced consistency check")
	Constancy constancy();

	@ConfSerialisers(DeviationEntrySerialiser.class)
	interface Constancy {

		@ConfKey("deviation-and-sample")
		@ConfComments({
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
		@ConfDefault.DefaultStrings({"30:10"})
		Set<DeviationEntry> deviationRequirements();

		@ConfKey("superdeviation-and-supersample")
		@ConfComments({
				"These are pairs of standard deviation percentages and sample counts",
				"",
				"These are conceptually similar to the previous. However, this measures the standard deviations between",
				"the standard deviations. Thus, it is called the \"super deviation\"."
		})
		@ConfDefault.DefaultStrings({"60:10"})
		Set<DeviationEntry> superDeviationRequirements();

	}

}

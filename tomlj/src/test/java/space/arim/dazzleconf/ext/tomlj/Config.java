/*
 * DazzleConf
 * Copyright © 2021 Anand Beh
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

package space.arim.dazzleconf.ext.tomlj;

import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

@ConfHeader("The tale of toml")
public interface Config {

	@AnnotationBasedSorter.Order(1)
	@ConfKey("do-you-like-toml")
	@ConfDefault.DefaultString("I do, but there aren't enough libraries which support it")
	String doYouLikeToml();

	@AnnotationBasedSorter.Order(2)
	@ConfKey("did-you-know-who-supports-it-now")
	@ConfDefault.DefaultString("well, not yet, but soon")
	String didYouKnowWhoSupportsItNow();

}

/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.model.common;

import java.time.LocalDate;

/**
 * @author Armin Reichert
 */
public class Score {
	private String title;
	private boolean visible;
	private boolean showContent;
	private int points;
	private int levelNumber;
	private LocalDate date;

	public Score(String title) {
		this.title = title;
		reset();
	}

	public void reset() {
		showContent = true;
		points = 0;
		levelNumber = 1;
		date = LocalDate.now();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String title() {
		return title;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setShowContent(boolean showContent) {
		this.showContent = showContent;
	}

	public boolean isShowContent() {
		return showContent;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int points() {
		return points;
	}

	public void setLevelNumber(int levelNumber) {
		this.levelNumber = levelNumber;
	}

	public int levelNumber() {
		return levelNumber;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDate date() {
		return date;
	}
}
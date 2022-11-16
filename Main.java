package mines;

import java.util.*;

public class Main {
	static final int SIZE = 9;
	static final String SPACE = "";
	static final String MINES = "X";
	static final String SAFE = ".";
	static final String MARKED = "*";
	static final String EXPLORE = "/";
	static final String INPUT_0 = "How many mines do you want on the field?";
	static final String INPUT_1 = "Set/unset mine marks or claim a cell as free:";
	static final String INPUT_2 = "Congratulations! You found all the mines!";
	static final String INPUT_3 = "There is a number here!";
	static final String INPUT_4 = "You stepped on a mine and failed!";
	static final String INPUT_5 = "There is a mine maked here!";
	static final String FREE = "free";
	static final String MINE = "mine";

	public static void main(String[] args) {
		// write your code here ...
		String[][] fields = new String[SIZE][SIZE];
		String[][] fields_copy = new String[SIZE][SIZE];
		String[][] fields_blank = new String[SIZE][SIZE];
		for(int i = 0; i < SIZE; i++) {
			Arrays.fill(fields[i], SAFE);
			Arrays.fill(fields_copy[i], SAFE);
			Arrays.fill(fields_blank[i], SAFE);
		}

		System.out.print(INPUT_0);
		Scanner sc = new Scanner(System.in);
		int initMineSize = sc.nextInt();
		
		showTheFields(fields_blank);

		int mineMarked = 0, freeCounter = 0, initCounter = 0;
		boolean hasInit = false;
		int rdnCol, rdnRow;
		do {
			System.out.println(INPUT_1);
			int inputCol = sc.nextInt() - 1;
			int inputRow = sc.nextInt() - 1;
			String freeOrMine = sc.next();
			
			if (!hasInit && MINE.equals(freeOrMine)) { // free making mine
				fields_blank[inputRow][inputCol] = isMarked(fields_blank[inputRow][inputCol]) ? SAFE : MARKED;
				showTheFields(fields_blank);
				continue;
			} else {
				if (!hasInit) {
					hasInit = true;
					// mine random initial for the first times free
					while (initCounter < initMineSize) {
						rdnCol = getRanNum(SIZE);
						rdnRow = getRanNum(SIZE);
						if (isFirstFreePoint(rdnCol,rdnRow,inputCol,inputRow)) 
							continue;
						if (isSafe(fields[rdnRow][rdnCol])) {  // it should not duplicate by same random couple number
							fields[rdnRow][rdnCol] = MINES;
							initCounter++;
						}
					}
					// Create mine number around a mine
					for (int rowIter = 0; rowIter < fields.length; rowIter++) {
						for (int colIter = 0; colIter < fields[rowIter].length; colIter++) {
							if (isSafe(fields[rowIter][colIter])) {
								fields_copy[rowIter][colIter] = countMinesAroundPot(fields, rowIter, colIter);
							}
							if (isMarked(fields_blank[rowIter][colIter])) {
								switch (fields[rowIter][colIter]) {
									case MINES: { // a lucky mark
										fields_copy[rowIter][colIter] = MARKED;
										mineMarked++;
										break;
									}
									default: { // an unlucky mark
										if (isSafe(fields_copy[rowIter][colIter])) { // mean it should not a number
											fields_copy[rowIter][colIter] = MARKED;
										}
										mineMarked--;
										break;
									}
								}
							}
						}
					}
					freeCounter += freeAPoint(fields_blank, fields_copy, fields, inputCol, inputRow);
				} else {
					if (MINE.equals(freeOrMine)) {
						switch (fields_copy[inputRow][inputCol]) {
							case SAFE: {
								fields_copy[inputRow][inputCol] = MARKED;
								mineMarked = isMine(fields[inputRow][inputCol]) ? ++mineMarked : --mineMarked;
								break;
							}
							case MARKED: {
								fields_copy[inputRow][inputCol] = SAFE;
								mineMarked = isMine(fields[inputRow][inputCol]) ? --mineMarked : mineMarked;
								break;
							}
							default: {
								break;
							}
						}
						fields_blank[inputRow][inputCol] = isMarked(fields_blank[inputRow][inputCol]) ? SAFE : MARKED;
					} else {
						// game over
						if (isMine(fields[inputRow][inputCol])) {
							fields_blank[inputRow][inputCol] = fields[inputRow][inputCol];
							showTheFields(fields_blank);
							System.out.println(INPUT_4);
							break;
						}
						freeCounter += freeAPoint(fields_blank, fields_copy, fields, inputCol, inputRow);
					}
				}
			}
			if (mineMarked == initMineSize || freeCounter + initMineSize == SIZE * SIZE) {
				break;
			} else {
				//showTheFields(fields);
				//showTheFields(fields_copy);
				showTheFields(fields_blank);
			}
		} while (true);

		if (mineMarked == initMineSize || freeCounter + initMineSize == SIZE * SIZE) {
			showTheFields(fields_blank);
			System.out.println(INPUT_2);
		}
		sc.close();
	}

	private static int freeAPoint(String[][] fields_blank, String[][] fields_copy, String[][] fields, int colx, int rowy) {
		int freeSize, openPointsCounter = 0;
		List<Point> freePoints = new ArrayList<>();
		Set<Point> numberPoints = new HashSet<>();
		while(true) {
			freeSize = freePoints.size();
//			System.out.println("1: " + freeSize);
			freePoints = getPointAround(freePoints, null, colx, rowy, fields_copy, fields);
			if (freeSize == freePoints.size() // no more free points
					|| freePoints.size() == 1) { // has number around || miner next to it
				getPointAround(freePoints, numberPoints, colx, rowy, fields_copy, fields);
//				System.out.println("4: " + (freePoints.size()));
//				System.out.println("5: " + (numberPoints.size()));
				break;
			}
//			System.out.println("2: " + (freePoints.size()));
//			System.out.println("3: " + (numberPoints.size()));
		};
		for (Point point : freePoints) {
			if (!isExplore(fields_blank[point.row()][point.col()])) {
				fields_blank[point.row()][point.col()] = EXPLORE;
				System.out.println("+");
				openPointsCounter++;
			}
		}
//		System.out.println("--------------------");
		for (Point point : numberPoints) {
			if (!isNumber(fields_blank[point.row()][point.col()])) {
				fields_blank[point.row()][point.col()] = point.sign();
				System.out.println("+");
				openPointsCounter++;
			}
		}
//		System.out.println("6: " + openPointsCounter);
		return openPointsCounter;
	}

	private static boolean isFirstFreePoint(int col, int row, int x, int y) {
		int addRow = Math.min(row + 1, SIZE - 1);
		int addCol = Math.min(col + 1, SIZE - 1);
		int subRow = Math.max(row - 1, 0);
		int subCol = Math.max(col - 1, 0);
		if (row == y && col == x // itself
				|| subRow == y && subCol == x // all 8/5/3 points around it
				|| subRow == y && col == x 
				|| subRow == y && addCol == x
				|| row == y && addCol == x 
				|| row == y && subCol == x 
				|| addRow == y && addCol == x 
				|| addRow == y && col == x
				|| addRow == y && subCol == x) {
			return true;
		}
		return false;
	}

	private static List<Point> getPointAround(List<Point> freePoints, Set<Point> numberPoints, int col, int row, String[][] fields_copy, String[][] fields) {
        if (freePoints.size() == 0 && !isNumber(fields_copy[row][col])) {
        	freePoints.add(new Point(row, col, fields_copy[row][col]));
        } else if (isNumber(fields_copy[row][col])) {
        	if (numberPoints != null) 
        		numberPoints.add(new Point(row, col, fields_copy[row][col]));
			return freePoints;
        }
    	int addRow,addCol,subRow,subCol,newRow,newCol;
        int size = freePoints.size();
        
        for (int i = 0; i < size; i++) {
        	newRow = freePoints.get(i).row();
        	newCol = freePoints.get(i).col();
        	addRow = Math.min(newRow + 1, SIZE - 1);
            addCol = Math.min(newCol + 1, SIZE - 1);
            subRow = Math.max(newRow - 1, 0);
            subCol = Math.max(newCol - 1, 0);
            if (newRow == 0) {
            	if (newCol == 0) {
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, addCol)) { freePoints.add(new Point(addRow, addCol, null)); fields_copy[addRow][addCol] = EXPLORE; }

            		if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, addCol)) { numberPoints.add(new Point(addRow, addCol, fields_copy[addRow][addCol]));}
            	} else if (newCol == SIZE - 1)  {
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, subCol)) { freePoints.add(new Point(addRow, subCol, null)); fields_copy[addRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, subCol)) { numberPoints.add(new Point(addRow, subCol, fields_copy[addRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
				} else   {
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, addCol)) { freePoints.add(new Point(addRow, addCol, null)); fields_copy[addRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, subCol)) { freePoints.add(new Point(addRow, subCol, null)); fields_copy[addRow][subCol] = EXPLORE; }

            		if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, addCol)) { numberPoints.add(new Point(addRow, addCol, fields_copy[addRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, subCol)) { numberPoints.add(new Point(addRow, subCol, fields_copy[addRow][subCol]));}
				}
            } else if (newRow == SIZE - 1) {
            	if (newCol == 0) {
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, addCol)) { freePoints.add(new Point(subRow, addCol, null)); fields_copy[subRow][addCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, addCol)) { numberPoints.add(new Point(subRow, addCol, fields_copy[subRow][addCol]));}
				} else if (newCol == SIZE - 1)   {
            		if (check(fields_copy, fields, subRow, subCol)) { freePoints.add(new Point(subRow, subCol, null)); fields_copy[subRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, subRow, subCol)) { numberPoints.add(new Point(subRow, subCol, fields_copy[subRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
				} else {
            		if (check(fields_copy, fields, subRow, subCol)) { freePoints.add(new Point(subRow, subCol, null)); fields_copy[subRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, addCol)) { freePoints.add(new Point(subRow, addCol, null)); fields_copy[subRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, subRow, subCol)) { numberPoints.add(new Point(subRow, subCol, fields_copy[subRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, addCol)) { numberPoints.add(new Point(subRow, addCol, fields_copy[subRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
				}
            } else {
            	if (newCol == 0) {
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, addCol)) { freePoints.add(new Point(subRow, addCol, null)); fields_copy[subRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, addCol)) { freePoints.add(new Point(addRow, addCol, null)); fields_copy[addRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, addCol)) { numberPoints.add(new Point(subRow, addCol, fields_copy[subRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, addCol)) { numberPoints.add(new Point(addRow, addCol, fields_copy[addRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
				} else if (newCol == SIZE - 1) {
            		if (check(fields_copy, fields, subRow, subCol)) { freePoints.add(new Point(subRow, subCol, null)); fields_copy[subRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, subCol)) { freePoints.add(new Point(addRow, subCol, null)); fields_copy[addRow][subCol] = EXPLORE; }

					if (numberPoints != null && isNumber(fields_copy, subRow, subCol)) { numberPoints.add(new Point(subRow, subCol, fields_copy[subRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, subCol)) { numberPoints.add(new Point(addRow, subCol, fields_copy[addRow][subCol]));}
				} else {
            		if (check(fields_copy, fields, subRow, subCol)) { freePoints.add(new Point(subRow, subCol, null)); fields_copy[subRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, newCol)) { freePoints.add(new Point(subRow, newCol, null)); fields_copy[subRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, subRow, addCol)) { freePoints.add(new Point(subRow, addCol, null)); fields_copy[subRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, addCol)) { freePoints.add(new Point(newRow, addCol, null)); fields_copy[newRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, newRow, subCol)) { freePoints.add(new Point(newRow, subCol, null)); fields_copy[newRow][subCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, addCol)) { freePoints.add(new Point(addRow, addCol, null)); fields_copy[addRow][addCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, newCol)) { freePoints.add(new Point(addRow, newCol, null)); fields_copy[addRow][newCol] = EXPLORE; }
            		if (check(fields_copy, fields, addRow, subCol)) { freePoints.add(new Point(addRow, subCol, null)); fields_copy[addRow][subCol] = EXPLORE; }

            		if (numberPoints != null && isNumber(fields_copy, subRow, subCol)) { numberPoints.add(new Point(subRow, subCol, fields_copy[subRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, newCol)) { numberPoints.add(new Point(subRow, newCol, fields_copy[subRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, subRow, addCol)) { numberPoints.add(new Point(subRow, addCol, fields_copy[subRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, addCol)) { numberPoints.add(new Point(newRow, addCol, fields_copy[newRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, newRow, subCol)) { numberPoints.add(new Point(newRow, subCol, fields_copy[newRow][subCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, addCol)) { numberPoints.add(new Point(addRow, addCol, fields_copy[addRow][addCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, newCol)) { numberPoints.add(new Point(addRow, newCol, fields_copy[addRow][newCol]));}
            		if (numberPoints != null && isNumber(fields_copy, addRow, subCol)) { numberPoints.add(new Point(addRow, subCol, fields_copy[addRow][subCol]));}
				}
            }
        }
        return freePoints;
	}

	private static void showTheFields(String[][] fields) {
		System.out.print(SPACE.equals(" ") ? " | 1 2 3 4 5 6 7 8 9|\n-| - - - - - - - - -|\n" : " |123456789|\n-|---------|\n"); // two version
		int i = 0;
		for (String[] field : fields) {
			System.out.print(++i + "|");
			for (String s : field) {
				System.out.print(SPACE);
				System.out.print(s);
			}
			System.out.print("|\n");
		}
		System.out.println(SPACE.equals(" ") ? "-| - - - - - - - - -|\n" : "-|---------|\n");
	}

	private static int getRanNum(int b) {
		return new Random().nextInt(b);
	}

	private static String countMinesAroundPot(String[][] fields, int row, int col) {
		int count = 0;
		int addRow = Math.min(row + 1, SIZE - 1);
		int addCol = Math.min(col + 1, SIZE - 1);
		int subRow = Math.max(row - 1, 0);
		int subCol = Math.max(col - 1, 0);

		if (row == 0) {
			if (col == 0) {
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, addRow, col)) count++;
				if (isMine(fields, addRow, addCol)) count++;
			} else if (col == SIZE - 1) {
				if (isMine(fields, row, subCol)) count++;
				if (isMine(fields, addRow, subCol)) count++;
				if (isMine(fields, addRow, col)) count++;
			} else {
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, row, subCol)) count++;
				if (isMine(fields, addRow, addCol)) count++;
				if (isMine(fields, addRow, col)) count++;
				if (isMine(fields, addRow, subCol)) count++;
			}
		} else if (row == SIZE - 1) {
			if (col == 0) {
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, subRow, addCol)) count++;
			} else if (col == SIZE - 1) {
				if (isMine(fields, subRow, subCol)) count++;
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, row, subCol)) count++;
			} else {
				if (isMine(fields, subRow, subCol)) count++;
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, subRow, addCol)) count++;
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, row, subCol)) count++;
			}
		} else {
			if (col == 0) {
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, subRow, addCol)) count++;
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, addRow, addCol)) count++;
				if (isMine(fields, addRow, col)) count++;
			} else if (col == SIZE - 1) {
				if (isMine(fields, subRow, subCol)) count++;
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, row, subCol)) count++;
				if (isMine(fields, addRow, col)) count++;
				if (isMine(fields, addRow, subCol)) count++;
			} else {
				if (isMine(fields, subRow, subCol)) count++;
				if (isMine(fields, subRow, col)) count++;
				if (isMine(fields, subRow, addCol)) count++;
				if (isMine(fields, row, addCol)) count++;
				if (isMine(fields, row, subCol)) count++;
				if (isMine(fields, addRow, addCol)) count++;
				if (isMine(fields, addRow, col)) count++;
				if (isMine(fields, addRow, subCol)) count++;
			}
		}
		return count > 0 ? "" + count : SAFE;
	}
	
	private static boolean isNumber(String[][] fields, int row, int col) {
		return isNumber(fields[row][col]);
	}
	private static boolean isNumber(String s) {
		return s.matches("\\d");
	}
	private static boolean isMine(String[][] fields, int row, int col) {
		return isMine(fields[row][col]);
	}
	private static boolean isMine(String s) {
		return s.equals(MINES);
	}
	private static boolean isExplore(String[][] fields, int row, int col) {
		return isExplore(fields[row][col]);
	}
	private static boolean isExplore(String s) {
		return s.equals(EXPLORE);
	}
	private static boolean isSafe(String s) {
		return s.equals(SAFE);
	}
	private static boolean isMarked(String s) {
		return s.equals(MARKED);
	}
	private static boolean check(String[][] fields_copy, String[][] fields, int row, int col) {
		return !isExplore(fields_copy, row, col) && !isNumber(fields_copy, row, col) && !isMine(fields, row, col);
	}
}

record Point(int row, int col, String sign) {};
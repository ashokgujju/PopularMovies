package com.ashok.popularmovies.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by ashok on 27/2/17.
 */

@Database(version = MovieDatabase.VERSION)
public class MovieDatabase {
    public static final int VERSION = 1;

    @Table(MovieColumns.class)
    public static final String MOVIES = "movies";
}

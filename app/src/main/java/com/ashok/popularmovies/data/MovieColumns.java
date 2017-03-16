package com.ashok.popularmovies.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by ashok on 27/2/17.
 */

public class MovieColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String POSTER_PATH = "poster_path";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String OVERVIEW = "overview";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String RELEASE_DATE = "release_date";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String TITLE = "title";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String VOTE_AVERAGE = "vote_average";
}

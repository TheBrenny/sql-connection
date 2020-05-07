# sql-connection

sql-connection is a Java library/wrapper for dealing with sql connections and queries.

## Installation

Either:
 - Download the library, export as a JAR, and add it to the buildpath/classpath.
 - Download the library, and add the files to your project.

## Usage

```java
import com.thebrenny.sqlconnection.*;

public static void main(String[] args) {
	SQLConnection sql = new SQLConnection("//db_url/", "db_to_use", "user", "pass");
	Query.Result r = sql.select("*").from("Players").where("score", ">=", 25).execute();
	r.getRowData();
}
```

## Contributing
Did something done did not okay? Submit a PR!

Did something not be there but you thought it would? Submit a PR!

Did something be not in your coding zone? Submit an issue!

## License
Also found in [license.txt](license.txt).
<hr>
MIT License

Copyright (c) 2020 Jarod Brennfleck

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
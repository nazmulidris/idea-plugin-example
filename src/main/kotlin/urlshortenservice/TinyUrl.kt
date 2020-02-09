/*
 * Copyright 2020 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package urlshortenservice

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

class TinyUrl : ShortenUrlService {
  /**
   * More info:
   * - `curl https://tinyurl.com/api-create.php?url="{query}"`
   * - https://stackoverflow.com/questions/724043/http-url-address-encoding-in-java
   * - https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
   */
  override fun shorten(longUrl: String): String {

    val url: URL = URL(longUrl)

    // println("arg = $arg")

    // with(url) {
    //     println("protocol = $protocol")
    //     println("userInfo = $userInfo")
    //     println("host     = $host")
    //     println("port     = $port")
    //     println("path     = $path")
    //     println("query    = $query")
    //     println("ref      = $ref")
    // }

    val uri: URI = with(url) { URI(protocol, userInfo, host, port, path, query, ref) }
    val encodedUri: String = uri.toURL().toString()

    // println(encodedUri)

    val tinyUrl = "https://tinyurl.com/api-create.php?url=${URLEncoder.encode(encodedUri, "UTF-8")}"

    // println(tinyUrl)

    val connection = URL(tinyUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    val reader = BufferedReader(InputStreamReader(connection.inputStream))

    val response = StringBuilder()

    var line: String? = ""

    while (line != null) {
      line = reader.readLine()
      line?.apply { response.append(this) }
    }

    reader.close()

    return response.toString()

  }

}

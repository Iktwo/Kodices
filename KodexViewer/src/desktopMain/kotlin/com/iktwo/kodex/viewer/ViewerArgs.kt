package com.iktwo.kodex.viewer

/** What the command line and the environment ask for. */
sealed interface ViewerArgs {
    data class Start(
        val config: ViewerConfig,
    ) : ViewerArgs

    data object Help : ViewerArgs

    data class Invalid(
        val message: String,
    ) : ViewerArgs
}

internal const val ENV_URL = "KODEX_URL"
internal const val ENV_TOKEN = "KODEX_TOKEN"
internal const val DEFAULT_URL = "http://localhost:8090"

internal val USAGE = """
    Usage: KodexViewer [--url <address>] [--token <token>]

      --url <address>    The server to connect to, e.g. http://127.0.0.1:8089. Default: ${'$'}$ENV_URL.
      --token <token>    The server's bearer token. Default: ${'$'}$ENV_TOKEN. Prefer the environment
                         variable: arguments are visible to every user in the process list.
      --help             Show this.

    With an address the viewer connects at once; without one it opens the connect form.
""".trimIndent()

/**
 * Arguments win over the environment. An address from either makes the viewer connect at once;
 * with neither it opens the form on [DEFAULT_URL].
 */
internal fun parseViewerArgs(
    args: List<String>,
    env: (String) -> String?,
): ViewerArgs {
    var url: String? = null
    var token: String? = null

    val iterator = args.iterator()
    while (iterator.hasNext()) {
        val arg = iterator.next()
        val name = arg.substringBefore('=')
        val inline = arg.substringAfter('=', missingDelimiterValue = "").takeIf { '=' in arg }

        when (name) {
            "--help", "-h" -> {
                return ViewerArgs.Help
            }

            "--url", "--token" -> {
                val value = inline
                    ?: if (iterator.hasNext()) iterator.next() else return ViewerArgs.Invalid("$name needs a value")
                if (name == "--url") url = value else token = value
            }

            else -> {
                return ViewerArgs.Invalid("Unknown argument: $name")
            }
        }
    }

    val address = (url ?: env(ENV_URL))?.trim()?.takeIf { it.isNotEmpty() }

    return ViewerArgs.Start(
        ViewerConfig(
            serverUrl = address ?: DEFAULT_URL,
            token = (token ?: env(ENV_TOKEN))?.trim().orEmpty(),
            autoConnect = address != null,
        ),
    )
}

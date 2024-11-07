package dev.aikido.agent_api.vulnerabilities.shell_injection;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ShellCommandsRegex {
    private static final List<String> COMMANDS = Arrays.asList(
            "sleep", "shutdown", "reboot", "poweroff", "halt", "ifconfig", "chmod",
            "chown", "ping", "ssh", "scp", "curl", "wget", "telnet", "kill", "killall",
            "rm", "mv", "cp", "touch", "echo", "cat", "head", "tail", "grep", "find",
            "awk", "sed", "sort", "uniq", "wc", "ls", "env", "ps", "who", "whoami",
            "id", "w", "df", "du", "pwd", "uname", "hostname", "netstat", "passwd",
            "arch", "printenv", "logname", "pstree", "hostnamectl", "set", "lsattr",
            "killall5", "dmesg", "history", "free", "uptime", "finger", "top", "shopt",
            ":"
    );
    private static final List<String> PATH_PREFIXES = Arrays.asList(
            "/bin/", "/sbin/", "/usr/bin/", "/usr/sbin/", "/usr/local/bin/", "/usr/local/sbin/"
    );

    public static final Pattern COMMANDS_REGEX = Pattern.compile(
            "([/.]*(" + String.join("|", PATH_PREFIXES) + ")?(" +
                    String.join("|", COMMANDS) + "))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
}

export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
export LANG=de_DE.utf-8
export APP_ENV=development
#lein trampoline ring server-headless 8080
#lein trampoline run -m cblog.core
lein2 trampoline run -m cblog.core

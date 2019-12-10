# https://gist.github.com/cdown/1163649

###########################
# URL encode a string     #
# Arguments:              #
#   string to be encoded  #
###########################
urlencode()
{
    local length="${#1}"
    for (( i = 0; i < length; i++ ))
    do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done
}

###########################
# URL decode a string     #
# Arguments:              #
#   string to be decoded  #
###########################
urldecode()
{
    local url_encoded="${1//+/ }"
    printf '%b' "${url_encoded//%/\\x}"
}



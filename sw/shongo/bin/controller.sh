#!/bin/bash

cd `dirname $0`/../
VERSION=`cat pom.xml | grep '<shongo.version>' | sed -e 's/.\+>\(.\+\)<.\+/\1/g'`

hash jsawk 2>/dev/null || {
    echo >&2 "Jsawk is required but it's not installed.";
    echo >&2 "You can download it here [https://github.com/micha/jsawk].";
    exit 1;
}

case $1 in
    reservation-requests)
        shift
        for id in $(bin/client-cli.sh --connect $1 --root --scripting --cmd "list-reservation-requests" | jsawk -n "out(this.id)"); do

            # Get reservation request id only if it has owner
            id=$(bin/client-cli.sh --connect $1 --root --scripting --cmd "list-acl -entity $id -role OWNER" | jsawk 'RS=RS.concat(this.entity)' -a 'return RS[0]')
            if [ -n "$id" ]; then
                echo
                bin/client-cli.sh --connect $1 --root --scripting --cmd "get-reservation-request $id" | sed "s/^{$/create-reservation-request {/g" | grep -v "\"id\""
            fi
        done;
        echo
        ;;
    *)
        java -jar controller/target/controller-$VERSION.jar "$@"
        ;;
esac

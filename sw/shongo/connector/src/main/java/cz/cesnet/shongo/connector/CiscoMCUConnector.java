package cz.cesnet.shongo.connector;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.*;
import cz.cesnet.shongo.api.util.Address;
import cz.cesnet.shongo.connector.api.*;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A connector for Cisco TelePresence MCU.
 * <p/>
 * Uses HTTPS (only).
 * <p/>
 * Works using API 2.9. The following Cisco TelePresence products are supported, provided they are running MCU version
 * 4.3 or later:
 * - Cisco TelePresence MCU 4200 Series
 * - Cisco TelePresence MCU 4500 Series
 * - Cisco TelePresence MCU MSE 8420
 * - Cisco TelePresence MCU MSE 8510
 * <p/>
 *
 * @author Ondrej Bouda <ondrej.bouda@cesnet.cz>
 */
public class CiscoMCUConnector extends AbstractConnector implements MultipointService
{
    private static Logger logger = LoggerFactory.getLogger(CiscoMCUConnector.class);

    private static final int STRING_MAX_LENGTH = 31;

    /**
     * A safety limit for number of enumerate pages.
     * <p/>
     * When enumerating some objects, the MCU returns the results page by page. To protect the connector from infinite
     * loop when the device gives an incorrect results, there is a limit on the number of pages the connector processes.
     * If this limit is reached, an exception is thrown (i.e., no part of the result may be used), as such a behaviour
     * is considered erroneous.
     */
    private static final int ENUMERATE_PAGES_LIMIT = 1000;

    /**
     * An example of interaction with the device.
     * <p/>
     * Just for debugging purposes.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, CommandException, CommandUnsupportedException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        final String address;
        final String username;
        final String password;

        if (args.length > 0) {
            address = args[0];
        }
        else {
            System.out.print("address: ");
            address = in.readLine();
        }

        if (args.length > 1) {
            username = args[1];
        }
        else {
            System.out.print("username: ");
            username = in.readLine();
        }

        if (args.length > 2) {
            password = args[2];
        }
        else {
            System.out.print("password: ");
            password = in.readLine();
        }

        CiscoMCUConnector conn = new CiscoMCUConnector();
        conn.connect(Address.parseAddress(address), username, password);

        // gatekeeper status
//        Map<String, Object> gkInfo = conn.exec(new Command("gatekeeper.query"));
//        System.out.println("Gatekeeper status: " + gkInfo.get("gatekeeperUsage"));

        // test of getRoomList() command
//        Collection<RoomInfo> roomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomInfo room : roomList) {
//            System.out.printf("  - %s (%s, started at %s, owned by %s)\n", room.getName(), room.getType(),
//                    room.getStartDateTime(), room.getOwner());
//        }

        // test that the second enumeration query fills data that has not changed and therefore were not transferred
//        Command enumParticipantsCmd = new Command("participant.enumerate");
//        enumParticipantsCmd.setParameter("operationScope", new String[]{"currentState"});
//        enumParticipantsCmd.setParameter("enumerateFilter", "connected");
//        List<Map<String, Object>> participants = conn.execEnumerate(enumParticipantsCmd, "participants");
//        List<Map<String, Object>> participants2 = conn.execEnumerate(enumParticipantsCmd, "participants");

        // test that the second enumeration query fills data that has not changed and therefore were not transferred
//        Command enumConfCmd = new Command("conference.enumerate");
//        enumConfCmd.setParameter("moreThanFour", Boolean.TRUE);
//        enumConfCmd.setParameter("enumerateFilter", "completed");
//        List<Map<String, Object>> confs = conn.execEnumerate(enumConfCmd, "conferences");
//        List<Map<String, Object>> confs2 = conn.execEnumerate(enumConfCmd, "conferences");

        // test of getRoom() command
//        Room shongoTestRoom = conn.getRoom("shongo-test");
//        System.out.println("shongo-test room:");
//        System.out.println(shongoTestRoom);

        // test of deleteRoom() command
//        Collection<RoomInfo> roomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomInfo room : roomList) {
//            System.out.println(room);
//        }
//        System.out.println("Deleting 'shongo-test'");
//        conn.deleteRoom("shongo-test");
//        roomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomInfo room : roomList) {
//            System.out.println(room);
//        }

        // test of createRoom() method
//        Room newRoom = new Room("shongo-test9", 5);
//        newRoom.addAlias(new Alias(Technology.H323, AliasType.E164, "950087209"));
//        newRoom.setOption(Room.OPT_DESCRIPTION, "Shongo testing room");
//        newRoom.setOption(Room.OPT_LISTED_PUBLICLY, true);
//        String newRoomId = conn.createRoom(newRoom);
//        System.out.println("Created room " + newRoomId);
//        Collection<RoomInfo> roomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomInfo room : roomList) {
//            System.out.println(room);
//        }

        // test of bad caching
//        Room newRoom = new Room("shongo-testX", 5);
//        String newRoomId = conn.createRoom(newRoom);
//        System.out.println("Created room " + newRoomId);
//        Collection<RoomSummary> roomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomSummary roomSummary : roomList) {
//            System.out.println(roomSummary);
//        }
//        conn.deleteRoom(newRoomId);
//        System.out.println("Deleted room " + newRoomId);
//        Map<String, Object> atts = new HashMap<String, Object>();
//        atts.put(Room.NAME, "shongo-testing");
//        String changedRoomId = conn.modifyRoom("shongo-test", atts, null);
//        Collection<RoomSummary> newRoomList = conn.getRoomList();
//        System.out.println("Existing rooms:");
//        for (RoomSummary roomSummary : newRoomList) {
//            System.out.println(roomSummary);
//        }
//        atts = new HashMap<String, Object>();
//        atts.put(Room.NAME, "shongo-test");
//        conn.modifyRoom(changedRoomId, atts, null);

        // test of modifyRoom() method
//        System.out.println("Modifying shongo-test");
//        Map<String, Object> atts = new HashMap<String, Object>();
//        atts.put(Room.NAME, "shongo-testing");
//        Map<Room.Option, Object> opts = new EnumMap<Room.Option, Object>(Room.Option.class);
//        opts.put(Room.Option.LISTED_PUBLICLY, false);
//        opts.put(Room.Option.PIN, "1234");
//        conn.modifyRoom("shongo-test", atts, opts);
//        Map<String, Object> atts2 = new HashMap<String, Object>();
//        atts2.put(Room.ALIASES, Collections.singletonList(new Alias(Technology.H323, AliasType.E164, "950087201")));
//        atts2.put(Room.NAME, "shongo-test");
//        conn.modifyRoom("shongo-testing", atts2, null);

        // test of listParticipants() method
//        System.out.println("Listing shongo-test room:");
//        Collection<RoomUser> shongoUsers = conn.listParticipants("shongo-test");
//        for (RoomUser ru : shongoUsers) {
//            System.out.println("  - " + ru.getUserId() + " (" + ru.getDisplayName() + ")");
//        }
//        System.out.println("Listing done");

        // user connect by alias
//        String ruId = conn.dialParticipant("shongo-test", new Alias(Technology.H323, AliasType.E164, "950081038"));
//        System.out.println("Added user " + ruId);
        // user connect by address
//        String ruId2 = conn.dialParticipant("shongo-test", "147.251.54.102");
        // user disconnect
//        conn.disconnectParticipant("shongo-test", "participant1");

//        System.out.println("All done, disconnecting");

        // test of modifyParticipant
//        Map<String, Object> attributes = new HashMap<String, Object>();
//        attributes.put(RoomUser.VIDEO_MUTED, Boolean.TRUE);
//        attributes.put(RoomUser.DISPLAY_NAME, "Ondrej Bouda");
//        conn.modifyParticipant("shongo-test", "3447", attributes);

        Room room = conn.getRoom("shongo-test");

        conn.disconnect();
    }


    /**
     * The default port number to connect to.
     */
    public static final int DEFAULT_PORT = 443;

    private XmlRpcClient client;

    private String authUsername;
    private String authPassword;

    /**
     * H.323 gatekeeper registration prefix - prefix added to room numericIds to get the full number under which the
     * room is callable.
     */
    private String gatekeeperRegistrationPrefix = null;


    // COMMON SERVICE

    /**
     * Connects to the MCU.
     * <p/>
     * Sets up the device URL where to send requests.
     * The communication protocol is stateless, though, so it just gets some info and does not hold the line.
     *
     * @param address  device address to connect to
     * @param username username for authentication on the device
     * @param password password for authentication on the device
     * @throws CommandException
     */
    @Override
    public void connect(Address address, String username, String password) throws CommandException
    {
        if (address.getPort() == Address.DEFAULT_PORT) {
            address.setPort(DEFAULT_PORT);
        }

        info.setDeviceAddress(address);

        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(getDeviceURL());
            // not standard basic auth - credentials are to be passed together with command parameters
            authUsername = username;
            authPassword = password;

            client = new XmlRpcClient();
            client.setConfig(config);

            // FIXME: remove, the production code should not trust any certificate
            try {
                setTrustAllCertificates();
            }
            catch (NoSuchAlgorithmException e) {
                logger.error("Error setting trust to all certificates", e);
            }
            catch (KeyManagementException e) {
                logger.error("Error setting trust to all certificates", e);
            }

            initSession();
            initDeviceInfo();
        }
        catch (MalformedURLException e) {
            throw new CommandException("Error constructing URL of the device.", e);
        }
        catch (CommandException e) {
            throw new CommandException("Error setting up connection to the device.", e);
        }

        info.setConnectionState(ConnectorInfo.ConnectionState.LOOSELY_CONNECTED);

    }

    private void initSession() throws CommandException
    {
        Command gkInfoCmd = new Command("gatekeeper.query");
        Map<String, Object> gkInfo = exec(gkInfoCmd);
        if (!gkInfo.get("gatekeeperUsage").equals("disabled")) {
            gatekeeperRegistrationPrefix = (String) gkInfo.get("registrationPrefix");
        }
    }

    private void initDeviceInfo() throws CommandException
    {
        Map<String, Object> device = exec(new Command("device.query"));
        DeviceInfo di = new DeviceInfo();

        di.setName((String) device.get("model"));

        String version = device.get("softwareVersion")
                + " (API: " + device.get("apiVersion")
                + ", build: " + device.get("buildVersion")
                + ")";
        di.setSoftwareVersion(version);

        di.setSerialNumber((String) device.get("serial"));

        info.setDeviceInfo(di);
    }

    /**
     * Configures the client to trust any certificate, without the need to have it in the keystore.
     * <p/>
     * Just a quick and dirty solution for certificate issues. The production solution should not use this method!
     * <p/>
     * Taken from http://ws.apache.org/xmlrpc/ssl.html
     */
    private void setTrustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager()
                {
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                        // Trust always
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                        // Trust always
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        // Create empty HostnameVerifier
        HostnameVerifier hv = new HostnameVerifier()
        {
            public boolean verify(String arg0, SSLSession arg1)
            {
                return true;
            }
        };

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    @Override
    public void disconnect() throws CommandException
    {
        // TODO: consider publishing feedback events from the MCU
        // no real operation - the communication protocol is stateless
        info.setConnectionState(ConnectorInfo.ConnectionState.DISCONNECTED);
        client = null; // just for sure the attributes are not used anymore
        gatekeeperRegistrationPrefix = null;
    }


    /**
     * Returns the URL on which to communicate with the device.
     *
     * @return URL for communication with the device
     */
    private URL getDeviceURL() throws MalformedURLException
    {
        // RPC2 is a fixed path given by Cisco, see the API docs
        return new URL("https", info.getDeviceAddress().getHost(), info.getDeviceAddress().getPort(), "RPC2");
    }

    /**
     * Sends a command to the device. Blocks until response to the command is complete.
     *
     * @param command a command to the device; note that some parameters may be added to the command
     * @return output of the command
     */
    private Map<String, Object> exec(Command command) throws CommandException
    {
        command.setParameter("authenticationUser", authUsername);
        command.setParameter("authenticationPassword", authPassword);
        Object[] params = new Object[]{command.getParameters()};
        try {
            return (Map<String, Object>) client.execute(command.getCommand(), params);
        }
        catch (XmlRpcException e) {
            throw new CommandException(e.getMessage());
        }
    }

    /**
     * Executes a command enumerating some objects.
     * <p/>
     * When possible (currently for commands conference.enumerate and participant.enumerate), caches the results and
     * asks just for the difference since previous call of the same command.
     * The caching is intentionally disabled for the autoAttendants.enumerate command, as the revisioning mechanism
     * seems to be broken on the device (it reports dead items even with the listAll parameter set to true), and either
     * way it generates short lists.
     *
     * @param command   command for enumerating the objects; note that some parameters may be added to the command
     * @param enumField the field within result containing the list of enumerated objects
     * @return list of objects from the enumField, each as a map from field names to values;
     *         the list is unmodifiable (so that it may be reused by the execEnumerate() method)
     * @throws CommandException
     */
    private List<Map<String, Object>> execEnumerate(Command command, String enumField) throws CommandException
    {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        // use revision numbers to get just the difference from the previous call of this command
        Integer lastRevision = prepareCaching(command);
        Integer currentRevision = null;

        for (int enumPage = 0; ; enumPage++) {
            // safety pages number check - to prevent infinite loop if the device does not work correctly
            if (enumPage >= ENUMERATE_PAGES_LIMIT) {
                String message = String
                        .format("Enumerate pages safety limit reached - the device gave more than %d result pages!",
                                ENUMERATE_PAGES_LIMIT);
                throw new CommandException(message);
            }

            // ask for data
            Map<String, Object> result = exec(command);
            // get the revision number of the first page - for using cache
            if (enumPage == 0) {
                currentRevision = (Integer) result.get("currentRevision"); // might not exist in the result and be null
            }

            // process data
            if (!result.containsKey(enumField)) {
                break; // no data at all
            }
            Object[] data = (Object[]) result.get(enumField);
            for (Object obj : data) {
                results.add((Map<String, Object>) obj);
            }

            // ask for more results, or break if that was all
            if (result.containsKey("enumerateID")) {
                command.setParameter("enumerateID", result.get("enumerateID"));
            }
            else {
                break; // that's all, folks
            }
        }

        if (currentRevision != null) {
            populateResultsFromCache(results, currentRevision, lastRevision, command, enumField);
        }

        return Collections.unmodifiableList(results);
    }

    private static RoomSummary extractRoomSummary(Map<String, Object> conference)
    {
        RoomSummary info = new RoomSummary();
        info.setIdentifier((String) conference.get("conferenceName"));
        info.setName((String) conference.get("conferenceName"));
        info.setDescription((String) conference.get("description"));
        String timeField = (conference.containsKey("startTime") ? "startTime" : "activeStartTime");
        info.setStartDateTime(new DateTime(conference.get(timeField)));
        return info;
    }


    /**
     * For string parameters, MCU accepts only strings of limited length.
     * <p/>
     * There are just a few exceptions to the limit. For the rest, this method ensures truncation with logging strings
     * that are longer.
     * <p/>
     * Constant <code>STRING_MAX_LENGTH</code> is used as the limit.
     *
     * @param str string to be (potentially) truncated
     * @return <code>str</code> truncated to the maximum length supported by the device
     */
    private static String truncateString(String str)
    {
        if (str.length() > STRING_MAX_LENGTH) {
            logger.warn(
                    "Too long string: '" + str + "', the device only supports " + STRING_MAX_LENGTH + "-character strings");
            str = str.substring(0, STRING_MAX_LENGTH);
        }
        return str;
    }

    //<editor-fold desc="RESULTS CACHING">

    /**
     * Prepares caching of result of the supplied command.
     *
     * @param command command to be issued; may be modified (some parameters regarding caching may be added)
     * @return the last revision when the same command was issued
     */
    private Integer prepareCaching(Command command)
    {
        Integer lastRevision = getCachedRevision(command);
        if (lastRevision != null) {
            command.setParameter("lastRevision", lastRevision);
            command.setParameter("listAll", Boolean.TRUE);
        }
        return lastRevision;
    }

    /**
     * Populates the results list - puts the original objects instead of item stubs.
     * <p/>
     * If there was a previous call to the same command, the changed items are just stubs in the new result set. To use
     * the results, this method populates all the stubs and puts the objects from the previous call in their place.
     *
     * @param results         list of results, some of which may be stubs; gets modified so that it contains no stubs
     * @param currentRevision the revision of this results
     * @param lastRevision    the revision of the previous call of the same command
     * @param command         the command called to get the supplied results
     * @param enumField       the field name from which the supplied results where taken within the command result
     */
    private void populateResultsFromCache(List<Map<String, Object>> results, Integer currentRevision,
            Integer lastRevision, Command command, String enumField)
    {
        // we got just the difference since lastRevision (or full set if this is the first issue of the command)
        final String cacheId = getCommandCacheId(command);

        if (lastRevision != null) {
            // fill the values that have not changed since lastRevision
            ListIterator<Map<String, Object>> iterator = results.listIterator();
            while (iterator.hasNext()) {
                Map<String, Object> item = iterator.next();

                if (isItemDead(item)) {
                    // from the MCU API: "The device will also never return a dead record if listAll is set to true."
                    // unfortunately, the buggy MCU still reports some items as dead even though listAll = true, so we
                    //   must remove them by ourselves (according to the API, a dead item should not have been ever
                    //   listed when listAll = true
                    iterator.remove();
                }
                else if (!hasItemChanged(item)) {
                    ResultsCache cache = resultsCache.get(cacheId);
                    iterator.set(cache.getItem(item));
                }
            }
        }

        // store the results and the revision number for the next time
        ResultsCache rc = resultsCache.get(cacheId);
        if (rc == null) {
            rc = new ResultsCache();
            resultsCache.put(cacheId, rc);
        }
        rc.store(currentRevision, results);
    }

    /**
     * Tells whether an item from a result of an enumeration command has been removed since last time the command was
     * issued.
     *
     * @param item an item from the resulting list
     * @return <code>true</code> if the item is marked as removed, <code>false</code> if not
     */
    private static boolean isItemDead(Map<String, Object> item)
    {
        // try directly the item "dead" attribute
        if (Boolean.TRUE.equals(item.get("dead"))) {
            return true;
        }

        // for some enumeration items (namely "participants"), the "dead" attribute might? (who knows, it shouldn't
        //   have been there for any result, since listAll=true) be listed in "currentState"
        @SuppressWarnings("unchecked")
        Map<String, Object> currentState = (Map<String, Object>) item.get("currentState");
        if (currentState != null && Boolean.TRUE.equals(currentState.get("dead"))) {
            return true;
        }

        return false; // not reported as dead
    }


    /**
     * Tells whether an item from a result of an enumeration command changed since last time the command was issued.
     *
     * @param item an item from the resulting list
     * @return <code>false</code> if the item is marked as not changed,
     *         <code>true</code> if the item is not marked as not changed
     */
    private static boolean hasItemChanged(Map<String, Object> item)
    {
        // try directly the item "changed" attribute
        if (Boolean.FALSE.equals(item.get("changed"))) {
            return false;
        }

        // for some enumeration items (namely "participants"), the "changed" attribute might be listed in "currentState"
        @SuppressWarnings("unchecked")
        Map<String, Object> currentState = (Map<String, Object>) item.get("currentState");
        if (currentState != null && Boolean.FALSE.equals(currentState.get("changed"))) {
            return false;
        }

        return true; // not reported as not changed
    }

    /**
     * Cache storing results from a single command.
     * <p/>
     * Stores the revision number and the corresponding result set.
     * <p/>
     * The items stored in the cache are compared just according to their unique identifiers. They may differ in other
     * attributes. The reason for this is to provide simple searching for an item - the cache is given an item which has
     * just its unique ID, and should find the previously stored, full version of the item. Hence comparing just
     * according to the IDs.
     * <p/>
     * If the item contains a "participantName" key, the value under this key is used as the item unique ID.
     * If the item contains a "conferenceName" key, the value under this key is used as the item unique ID.
     * Otherwise, only items with equal contents are considered equal.
     */
    private class ResultsCache
    {
        private class Item
        {

            private final Map<String, Object> contents;

            public Item(Map<String, Object> contents)
            {
                this.contents = contents;
            }

            public Map<String, Object> getContents()
            {
                return contents;
            }

            @Override
            public boolean equals(Object o)
            {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                Item item = (Item) o;

                final Object participantName = contents.get("participantName");
                if (participantName != null) {
                    return (participantName.equals(item.contents.get("participantName")));
                }

                final Object conferenceName = contents.get("conferenceName");
                if (conferenceName != null) {
                    return (conferenceName.equals(item.contents.get("conferenceName")));
                }

                return contents.equals(item.contents);
            }

            @Override
            public int hashCode()
            {
                final Object participantName = contents.get("participantName");
                if (participantName != null) {
                    return participantName.hashCode();
                }
                final Object conferenceName = contents.get("conferenceName");
                if (conferenceName != null) {
                    return conferenceName.hashCode();
                }
                return contents.hashCode();
            }

        }

        private int revision;

        private List<Item> results;

        public int getRevision()
        {
            return revision;
        }

        public Map<String, Object> getItem(Map<String, Object> item)
        {
            final Item it = new Item(item);
            // FIXME: optimize - should be O(1) rather than O(n)
            for (Item cachedItem : results) {
                if (cachedItem.equals(it)) {
                    return cachedItem.getContents();
                }
            }
            return null;
        }

        public void store(int revision, List<Map<String, Object>> results)
        {
            this.revision = revision;
            this.results = new ArrayList<Item>();
            for (Map<String, Object> res : results) {
                this.results.add(new Item(res));
            }
        }

    }

    /**
     * Cache of results of previous calls to commands supporting revision numbers.
     * Map of cache ID to previous results.
     */
    private Map<String, ResultsCache> resultsCache = new HashMap<String, ResultsCache>();

    /**
     * Returns the revision number of the previous call of the given command.
     * <p/>
     * The purpose of this method is to enable caching of previous calls and asking for just the difference since then.
     * <p/>
     * All the parameters of the command are considered, except enumerateID, lastRevision, and listAll.
     * <p/>
     * Note that the return value must be boxed, because the MCU API does not say anything about the revision numbers
     * issued by the device. So it may have any value, thus, we must recognize the special case by the null value.
     *
     * @param command a command which will be performed
     * @return revision number of the previous call of the given command,
     *         or null if the command has not been issued yet or does not support revision numbers
     */
    private Integer getCachedRevision(Command command)
    {
        if (command.getCommand().equals("autoAttendant.enumerate")) {
            return null; // disabled for the autoAttendant.enumerate command - it is broken on the device
        }
        String cacheId = getCommandCacheId(command);
        ResultsCache rc = resultsCache.get(cacheId);
        return (rc == null ? null : rc.getRevision());
    }

    private String getCommandCacheId(Command command)
    {
        final String[] ignoredParams = new String[]{
                "enumerateID", "lastRevision", "listAll", "authenticationUser", "authenticationPassword"
        };

        StringBuilder sb = new StringBuilder(command.getCommand());
ParamsLoop:
        for (Map.Entry<String, Object> entry : command.getParameters().entrySet()) {
            for (String ignoredParam : ignoredParams) {
                if (entry.getKey().equals(ignoredParam)) {
                    continue ParamsLoop; // the parameter is ignored
                }
            }
            sb.append(";");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        return sb.toString();
    }


    //</editor-fold>

    //<editor-fold desc="ROOM SERVICE">

    @Override
    public Collection<RoomSummary> getRoomList() throws CommandException
    {
        Command cmd = new Command("conference.enumerate");
        cmd.setParameter("moreThanFour", Boolean.TRUE);
        cmd.setParameter("enumerateFilter", "!completed");

        Collection<RoomSummary> rooms = new ArrayList<RoomSummary>();
        List<Map<String, Object>> conferences = execEnumerate(cmd, "conferences");
        for (Map<String, Object> conference : conferences) {
            RoomSummary info = extractRoomSummary(conference);
            rooms.add(info);
        }

        return rooms;
    }

    @Override
    public Room getRoom(String roomId) throws CommandException
    {
        Command cmd = new Command("conference.status");
        cmd.setParameter("conferenceName", truncateString(roomId));
        Map<String, Object> result = exec(cmd);

        Room room = new Room();
        room.setIdentifier((String) result.get("conferenceName"));
        room.setName((String) result.get("conferenceName"));
        room.setPortCount((Integer) result.get("maximumVideoPorts"));

        // aliases
        if (!result.get("numericId").equals("")) {
            Alias numAlias = new Alias(Technology.H323, AliasType.E164, (String) result.get("numericId"));
            room.addAlias(numAlias);
        }

        // options
        room.setOption(Room.Option.DESCRIPTION, result.get("description"));
        room.setOption(Room.Option.REGISTER_WITH_H323_GATEKEEPER, result.get("registerWithGatekeeper"));
        room.setOption(Room.Option.REGISTER_WITH_SIP_REGISTRAR, result.get("registerWithSIPRegistrar"));
        room.setOption(Room.Option.LISTED_PUBLICLY, !(Boolean) result.get("private"));
        room.setOption(Room.Option.ALLOW_CONTENT, result.get("contentContribution"));
        room.setOption(Room.Option.JOIN_AUDIO_MUTED, result.get("joinAudioMuted"));
        room.setOption(Room.Option.JOIN_VIDEO_MUTED, result.get("joinVideoMuted"));
        if (!result.get("pin").equals("")) {
            room.setOption(Room.Option.PIN, result.get("pin"));
        }
        room.setOption(Room.Option.START_LOCKED, result.get("startLocked"));
        room.setOption(Room.Option.CONFERENCE_ME_ENABLED, result.get("conferenceMeEnabled"));

        return room;
    }

    @Override
    public String createRoom(Room room) throws CommandException
    {
        Command cmd = new Command("conference.create");

        cmd.setParameter("customLayoutEnabled", Boolean.TRUE);

        cmd.setParameter("enforceMaximumAudioPorts", Boolean.TRUE);
        cmd.setParameter("maximumAudioPorts", 0); // audio-only participants are forced to use video slots
        cmd.setParameter("enforceMaximumVideoPorts", Boolean.TRUE);

        // defaults (may be overridden by specified room options
        cmd.setParameter("registerWithGatekeeper", Boolean.FALSE);
        cmd.setParameter("registerWithSIPRegistrar", Boolean.FALSE);
        cmd.setParameter("private", Boolean.TRUE);
        cmd.setParameter("contentContribution", Boolean.TRUE);
        cmd.setParameter("contentTransmitResolutions", "allowAll");
        cmd.setParameter("joinAudioMuted", Boolean.FALSE);
        cmd.setParameter("joinVideoMuted", Boolean.FALSE);
        cmd.setParameter("startLocked", Boolean.FALSE);
        cmd.setParameter("conferenceMeEnabled", Boolean.FALSE);

        setConferenceParametersByRoom(cmd, room);

        exec(cmd);

        return room.getName();
    }

    private void setConferenceParametersByRoom(Command cmd, Room room) throws CommandException
    {
        if (room.getName() != null) {
            cmd.setParameter("conferenceName", truncateString(room.getName()));
        }

        if (room.getPortCount() >= 0) {
            cmd.setParameter("maximumVideoPorts", room.getPortCount());
        }

        if (room.getAliases() != null) {
            cmd.setParameter("numericId", "");
            for (Alias alias : room.getAliases()) {
                if (alias.getTechnology() == Technology.H323 && alias.getType() == AliasType.E164) {
                    if (!cmd.getParameterValue("numericId").equals("")) {
                        // multiple number aliases
                        final String m = "The connector supports only one numeric H.323 alias, requested another: " + alias;
                        throw new CommandException(m);
                    }
                    // number of the room
                    String number = alias.getValue();
                    if (gatekeeperRegistrationPrefix != null) {
                        if (!number.startsWith(gatekeeperRegistrationPrefix)) {
                            throw new CommandException(
                                    String.format("Assigned numbers should be prefixed with %s, number %s given.",
                                            gatekeeperRegistrationPrefix, number));
                        }
                        number = number.substring(gatekeeperRegistrationPrefix.length());
                    }
                    cmd.setParameter("numericId", truncateString(number));
                }
                else {
                    throw new CommandException("Unrecognized alias: " + alias);
                }
            }
        }

        cmd.setParameter("durationSeconds", 0); // set the room forever

        // options
        setCommandRoomOption(cmd, room, "registerWithGatekeeper", Room.Option.REGISTER_WITH_H323_GATEKEEPER);
        setCommandRoomOption(cmd, room, "registerWithSIPRegistrar", Room.Option.REGISTER_WITH_SIP_REGISTRAR);
        if (room.hasOption(Room.Option.LISTED_PUBLICLY)) {
            cmd.setParameter("private", !(Boolean) room.getOption(Room.Option.LISTED_PUBLICLY));
        }
        setCommandRoomOption(cmd, room, "contentContribution", Room.Option.ALLOW_CONTENT);
        setCommandRoomOption(cmd, room, "joinAudioMuted", Room.Option.JOIN_AUDIO_MUTED);
        setCommandRoomOption(cmd, room, "joinVideoMuted", Room.Option.JOIN_VIDEO_MUTED);
        setCommandRoomOption(cmd, room, "pin", Room.Option.PIN);
        setCommandRoomOption(cmd, room, "description", Room.Option.DESCRIPTION);
        setCommandRoomOption(cmd, room, "startLocked", Room.Option.START_LOCKED);
        setCommandRoomOption(cmd, room, "conferenceMeEnabled", Room.Option.CONFERENCE_ME_ENABLED);
    }

    private static void setCommandRoomOption(Command cmd, Room room, String cmdParam, Room.Option roomOption)
    {
        if (room.hasOption(roomOption)) {
            Object value = room.getOption(roomOption);
            if (value instanceof String) {
                value = truncateString((String) value);
            }
            cmd.setParameter(cmdParam, value);
        }
    }

    @Override
    public String modifyRoom(Room room) throws CommandException
    {
        // build the command
        Command cmd = new Command("conference.modify");
        setConferenceParametersByRoom(cmd, room);
        // treat the name and new name of the conference
        cmd.setParameter("conferenceName", truncateString(room.getIdentifier()));
        if (room.isPropertyFilled(Room.NAME)) {
            cmd.setParameter("newConferenceName", truncateString(room.getName()));
        }
        if (room.isPropertyFilled(Room.PORT_COUNT)) {
            cmd.setParameter("maximumVideoPorts", room.getPortCount());
        }
        // Create/Update aliases
        for (Alias alias : room.getAliases()) {
            if (alias.getTechnology() == Technology.H323 && alias.getType() == AliasType.E164) {
                if (room.isPropertyItemMarkedAsNew(Room.ALIASES, alias)) {
                    // MCU only supports a single H323-E164 alias; if another is to be set, throw an exception
                    Room currentRoom = getRoom(room.getIdentifier());
                    for (Alias curAlias : currentRoom.getAliases()) {
                        if (curAlias.getTechnology() == Technology.H323 && curAlias.getType() == AliasType.E164) {
                            final String m = "The connector supports only one numeric H.323 alias, requested another: " + alias;
                            throw new CommandException(m);
                        }
                    }
                }
                cmd.setParameter("numericId", truncateString(alias.getValue()));
            }
        }
        // Delete aliases
        Set<Alias> aliasesToDelete = room.getPropertyItemsMarkedAsDeleted(Room.ALIASES);
        for (Alias alias : aliasesToDelete) {
            if (alias.getTechnology() == Technology.H323 && alias.getType() == AliasType.E164) {
                cmd.setParameter("numericId", "");
            }
        }
        // Create/Update options
        for (Room.Option option : room.getOptions().keySet()) {
            if (room.isPropertyItemMarkedAsNew(Room.OPTIONS, option)) {
                // TODO: new option
                logger.debug("New option {} = {}", option, room.getOption(option));
            }
            else {
                // TODO: modified option
                logger.debug("Modified option {} = {}", option, room.getOption(option));
            }

            if (option == Room.Option.DESCRIPTION) {
                cmd.setParameter("description", truncateString((String) room.getOption(Room.Option.DESCRIPTION)));
            }
        }
        // Delete aliases
        Set<Room.Option> optionsToDelete = room.getPropertyItemsMarkedAsDeleted(Room.OPTIONS);
        for (Room.Option option : optionsToDelete) {
            // TODO: delete option
            logger.debug("Delete option {}", option);
            if (option == Room.Option.DESCRIPTION) {
                cmd.setParameter("description", null);
            }
        }

        exec(cmd);

        if (room.isPropertyFilled(Room.NAME)) {
            // the room name changed - the room ID must change, too
            return room.getName();
        }
        else {
            return room.getIdentifier();
        }
    }

    @Override
    public void deleteRoom(String roomId) throws CommandException
    {
        Command cmd = new Command("conference.destroy");
        cmd.setParameter("conferenceName", truncateString(roomId));
        exec(cmd);
    }

    @Override
    public String exportRoomSettings(String roomId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public void importRoomSettings(String roomId, String settings) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    //</editor-fold>

    //<editor-fold desc="ROOM CONTENT SERVICE">

    @Override
    public void removeRoomContentFile(String roomId, String name) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public MediaData getRoomContent(String roomId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public void clearRoomContent(String roomId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public void addRoomContent(String roomId, String name, MediaData data)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    //</editor-fold>

    //<editor-fold desc="USER SERVICE">

    @Override
    public RoomUser getParticipant(String roomId, String roomUserId) throws CommandException
    {
        Command cmd = new Command("participant.status");
        identifyParticipant(cmd, roomId, roomUserId);
        cmd.setParameter("operationScope", new String[]{"currentState"});

        Map<String, Object> result = exec(cmd);

        return extractRoomUser(result);
    }

    @Override
    public String dialParticipant(String roomId, Alias alias) throws CommandException
    {
        return dialParticipant(roomId, alias.getValue());
    }

    @Override
    public String dialParticipant(String roomId, String address) throws CommandException
    {
        // FIXME: refine just as the createRoom() method - get just a RoomUser object and set parameters according to it

        // NOTE: adding participants as ad_hoc - the MCU autogenerates their IDs (but they are just IDs, not names),
        //       thus, commented out the following generation of participant names
//        String roomUserId = generateRoomUserId(roomId); // FIXME: treat potential race conditions; and it is slow...

        Command cmd = new Command("participant.add");
        cmd.setParameter("conferenceName", truncateString(roomId));
//        cmd.setParameter("participantName", truncateString(roomUserId));
        cmd.setParameter("address", truncateString(address));
        cmd.setParameter("participantType", "ad_hoc");
        cmd.setParameter("addResponse", Boolean.TRUE);

        Map<String, Object> result = exec(cmd);

        @SuppressWarnings("unchecked")
        Map<String, Object> participant = (Map<String, Object>) result.get("participant");
        if (participant == null) {
            return null;
        }
        else {
            return String.valueOf(participant.get("participantName"));
        }
    }

    /**
     * Generates a room user ID for a new user.
     *
     * @param roomId technology ID of the room to generate a new user ID for
     * @return a free roomUserId to be assigned (free in the moment of processing this method, might race condition with
     *         someone else)
     */
    private String generateRoomUserId(String roomId) throws CommandException
    {
        List<Map<String, Object>> participants;
        try {
            Command cmd = new Command("participant.enumerate");
            cmd.setParameter("operationScope", new String[]{"currentState"});
            participants = execEnumerate(cmd, "participants");
        }
        catch (CommandException e) {
            throw new CommandException("Cannot generate a new room user ID - cannot list current room users.", e);
        }

        // generate the new ID as maximal ID of present users increased by one
        int maxFound = 0;
        Pattern pattern = Pattern.compile("^participant(\\d+)$");
        for (Map<String, Object> part : participants) {
            if (!part.get("conferenceName").equals(roomId)) {
                continue;
            }
            Matcher m = pattern.matcher((String) part.get("participantName"));
            if (m.find()) {
                maxFound = Math.max(maxFound, Integer.parseInt(m.group(1)));
            }
        }

        return String.format("participant%d", maxFound + 1);
    }

    @Override
    public Collection<RoomUser> listParticipants(String roomId) throws CommandException
    {
        Command cmd = new Command("participant.enumerate");
        cmd.setParameter("operationScope", new String[]{"currentState"});
        cmd.setParameter("enumerateFilter", "connected");
        List<Map<String, Object>> participants = execEnumerate(cmd, "participants");

        List<RoomUser> result = new ArrayList<RoomUser>();
        for (Map<String, Object> part : participants) {
            if (!part.get("conferenceName").equals(roomId)) {
                continue; // not from this room
            }
            result.add(extractRoomUser(part));
        }

        return result;
    }

    /**
     * Extracts a room-user out of participant.enumerate or participant.status result.
     *
     * @param participant participant structure, as defined in the MCU API, command participant.status
     * @return room user extracted from the participant structure
     */
    private static RoomUser extractRoomUser(Map<String, Object> participant)
    {
        RoomUser ru = new RoomUser();

        ru.setUserId((String) participant.get("participantName"));
        ru.setRoomId((String) participant.get("conferenceName"));

        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) participant.get("currentState");

        ru.setDisplayName((String) state.get("displayName"));

        ru.setAudioMuted((Boolean) state.get("audioRxMuted"));
        ru.setVideoMuted((Boolean) state.get("videoRxMuted"));
        if (state.get("audioRxGainMode").equals("fixed")) {
            ru.setMicrophoneLevel((Integer) state.get("audioRxGainMillidB"));
        }
        ru.setJoinTime(new DateTime(state.get("connectTime")));

        // room layout
        if (state.containsKey("currentLayout")) {
            RoomLayout.VoiceSwitching vs;
            if (state.get("focusType").equals("voiceActivated")) {
                vs = RoomLayout.VoiceSwitching.VOICE_SWITCHED;
            }
            else {
                vs = RoomLayout.VoiceSwitching.NOT_VOICE_SWITCHED;
            }
            final Integer layoutIndex = (Integer) state.get("currentLayout");
            RoomLayout rl = RoomLayout.getByCiscoId(layoutIndex, RoomLayout.SPEAKER_CORNER, vs);

            ru.setLayout(rl);
        }
        return ru;
    }

    @Override
    public void modifyParticipant(String roomId, String roomUserId, Map<String, Object> attributes)
            throws CommandException
    {
        Command cmd = new Command("participant.modify");
        identifyParticipant(cmd, roomId, roomUserId);

        // NOTE: oh yes, Cisco MCU wants "activeState" for modify while for status, it gets "currentState"...
        cmd.setParameter("operationScope", "activeState");

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            String attName = attribute.getKey();
            Object attValue = attribute.getValue();

            if (attName.equals(RoomUser.DISPLAY_NAME)) {
                cmd.setParameter("displayNameOverrideValue", truncateString((String) attValue));
                cmd.setParameter("displayNameOverrideStatus", Boolean.TRUE); // for the value to take effect
            }
            else if (attName.equals(RoomUser.AUDIO_MUTED)) {
                cmd.setParameter("audioRxMuted", attValue);
            }
            else if (attName.equals(RoomUser.VIDEO_MUTED)) {
                cmd.setParameter("videoRxMuted", attValue);
            }
            else if (attName.equals(RoomUser.MICROPHONE_LEVEL)) {
                cmd.setParameter("audioRxGainMillidB", attValue);
                cmd.setParameter("audioRxGainMode", "fixed"); // for the value to take effect
            }
            else if (attName.equals(RoomUser.PLAYBACK_LEVEL)) {
                logger.info("Ignoring request to set PLAYBACK_LEVEL - Cisco MCU does not support it.");
            }
            else if (attName.equals(RoomUser.LAYOUT)) {
                RoomLayout layout = (RoomLayout) attValue;
                cmd.setParameter("focusType",
                        (layout.getVoiceSwitching() == RoomLayout.VoiceSwitching.VOICE_SWITCHED ? "voiceActivated" : "participant"));
                logger.info("Setting only voice-switching mode. The layout itself cannot be set by Cisco MCU.");
            }
            else {
                throw new IllegalArgumentException("Unknown RoomUser attribute: " + attName);
            }
        }

        exec(cmd);
    }

    @Override
    public void disconnectParticipant(String roomId, String roomUserId) throws CommandException
    {
        Command cmd = new Command("participant.remove");
        identifyParticipant(cmd, roomId, roomUserId);

        exec(cmd);
    }

    private void identifyParticipant(Command cmd, String roomId, String roomUserId)
    {
        cmd.setParameter("conferenceName", truncateString(roomId));
        cmd.setParameter("participantName", truncateString(roomUserId));
        // NOTE: it is necessary to identify a participant also by type; ad_hoc participants receive auto-generated
        //       numbers, so we distinguish the type by the fact whether the name is a number or not
        cmd.setParameter("participantType", (StringUtils.isNumeric(roomUserId) ? "ad_hoc" : "by_address"));
    }

    @Override
    public void enableContentProvider(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        // NOTE: it seems it is not possible to enable content using current API (2.9)
        throw new CommandUnsupportedException();
    }

    @Override
    public void disableContentProvider(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        // NOTE: it seems it is not possible to disable content using current API (2.9)
        throw new CommandUnsupportedException();
    }

    //</editor-fold>

    //<editor-fold desc="I/O SERVICE">

    @Override
    public void disableParticipantVideo(String roomId, String roomUserId) throws CommandException
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RoomUser.VIDEO_MUTED, Boolean.TRUE);

        modifyParticipant(roomId, roomUserId, attributes);
    }

    @Override
    public void enableParticipantVideo(String roomId, String roomUserId) throws CommandException
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RoomUser.VIDEO_MUTED, Boolean.FALSE);

        modifyParticipant(roomId, roomUserId, attributes);
    }

    @Override
    public void muteParticipant(String roomId, String roomUserId) throws CommandException
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RoomUser.AUDIO_MUTED, Boolean.TRUE);

        modifyParticipant(roomId, roomUserId, attributes);
    }

    @Override
    public void setParticipantMicrophoneLevel(String roomId, String roomUserId, int level) throws CommandException
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RoomUser.MICROPHONE_LEVEL, level);

        modifyParticipant(roomId, roomUserId, attributes);
    }

    @Override
    public void setParticipantPlaybackLevel(String roomId, String roomUserId, int level)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException();
    }

    @Override
    public void unmuteParticipant(String roomId, String roomUserId) throws CommandException
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RoomUser.AUDIO_MUTED, Boolean.FALSE);

        modifyParticipant(roomId, roomUserId, attributes);
    }

    //</editor-fold>

    //<editor-fold desc="RECORDING SERVICE">

    @Override
    public void deleteRecording(int recordingId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public void downloadRecording(String downloadURL, String targetPath)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public String getRecordingDownloadURL(int recordingId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public Collection<String> notifyParticipants(int recordingId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public int startRecording(String roomId, ContentType format, RoomLayout layout)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public void stopRecording(int recordingId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    //</editor-fold>

    //<editor-fold desc="MONITORING SERVICE">

    @Override
    public DeviceLoadInfo getDeviceLoadInfo() throws CommandException
    {
        Map<String, Object> health = exec(new Command("device.health.query"));
        Map<String, Object> status = exec(new Command("device.query"));

        DeviceLoadInfo info = new DeviceLoadInfo();
        info.setCpuLoad(((Integer) health.get("cpuLoad")).doubleValue());
        if (status.containsKey("uptime")) {
            info.setUptime((Integer) status.get("uptime")); // NOTE: 'uptime' not documented, but it is there
        }

        // NOTE: memory and disk usage not accessible via API

        return info;
    }

    @Override
    public UsageStats getUsageStats() throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    @Override
    public MediaData getReceivedVideoSnapshot(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO: call participant.status and use previewURL
    }

    @Override
    public MediaData getSentVideoSnapshot(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(); // TODO
    }

    //</editor-fold>

}

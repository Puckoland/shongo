package cz.cesnet.shongo.common;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * Represents a Date/Time of events that takes place periodically.
 *
 * @author Martin Srom
 */
public class PeriodicDateTime extends DateTime
{
    private AbsoluteDateTime start;

    private Period period;

    private AbsoluteDateTime end;

    private ArrayList<Rule> rules = new ArrayList<Rule>();

    /**
     * Constructs periodical date/time events. The first event takes place at start
     * and each other take places in given period.
     *
     * @param start
     * @param period
     */
    public PeriodicDateTime(AbsoluteDateTime start, Period period)
    {
        this(start, period, null);
    }

    /**
     * Constructs periodical date/time events. The first event takes place at start
     * and each other take places in given period. The last event takes place before
     * or equals to end.
     *
     * @param start
     * @param period
     */
    public PeriodicDateTime(AbsoluteDateTime start, Period period, AbsoluteDateTime end)
    {
        setStart(start);
        setPeriod(period);
        setEnd(end);
    }

    /**
     * Get date/time of the first periodic event.
     *
     * @return absolute data/time
     */
    public AbsoluteDateTime getStart()
    {
        return start;
    }

    /**
     * Set date/time of the first periodic event.
     *
     * @param start
     */
    public void setStart(AbsoluteDateTime start)
    {
        this.start = start;
    }

    /**
     * Get period of periodic events.
     *
     * @return period
     */
    public Period getPeriod()
    {
        return period;
    }

    /**
     * Set events period.
     *
     * @param period
     */
    public void setPeriod(Period period)
    {
        this.period = period;
    }

    /**
     * Get ending date/time after which the periodic events are not considered.
     *
     * @return absolute date/time
     */
    public AbsoluteDateTime getEnd()
    {
        return end;
    }

    /**
     * Set ending date/time after which the periodic events are not considered.
     * The ending date/time can be nice e.g., 31.12.2012, and for periodic events
     * on every Thursday the last will take place on 29.12.2012.
     *
     * @param end
     */
    public void setEnd(AbsoluteDateTime end)
    {
        this.end = end;
    }

    /**
     * Add a new rule for periodic date, that can add extra date/time
     * outside the periodicity or enable/disable periodic events for
     * specified interval.
     *
     * @param rule
     */
    public void addRule(Rule rule)
    {
        rules.add(rule);
    }

    /**
     * Add a new rule for periodic date/time.
     *
     * @param type        Type of rule
     * @param dateTime    Concrete date/time
     */
    public void addRule(RuleType type, AbsoluteDateTime dateTime)
    {
        addRule(new Rule(type, dateTime));
    }

    /**
     * Add a new rule for periodic date/time.
     *
     * @param type            Type of rule
     * @param dateTimeFrom    Start of interval
     * @param dateTimeTo      End of interval
     */
    public void addRule(RuleType type, AbsoluteDateTime dateTimeFrom, AbsoluteDateTime dateTimeTo)
    {
        addRule(new Rule(type, dateTimeFrom, dateTimeTo));
    }

    /**
     * Remove all rules
     */
    public void clearRules()
    {
        rules.clear();
    }

    /**
     * Get all rules
     *
     * @return rules
     */
    public Rule[] getRules()
    {
        return rules.toArray(new Rule[rules.size()]);
    }

    /**
     * Enumerate all periodic Date/Time events to array of absolute Date/Times.
     *
     * @return array of absolute Date/Times
     */
    AbsoluteDateTime[] enumerate()
    {
        return enumerate(null, null);
    }

    /**
     * Enumerate all periodic Date/Time events to array of absolute Date/Times.
     * Return only events that take place inside interval defined by from - to.
     *
     * @param from
     * @param to
     * @return array of absolute Date/Times
     */
    AbsoluteDateTime[] enumerate(AbsoluteDateTime from, AbsoluteDateTime to)
    {
        throw new NotImplementedException();
    }

    @Override
    public AbsoluteDateTime getEarliest()
    {
        throw new NotImplementedException();
    }

    /**
     * Periodic date/time rule type
     *
     * @author Martin Srom
     */
    public enum RuleType
    {
        /** Represents a rule that will add new event outside periodicity. */
        Extra,

        /** Represents a rule for enabling events by concrete date/time or by interval from - to */
        Enable,

        /** Represents a rule for disabling events by concrete date/time or by interval from - to */
        Disable
    }

    /**
     * Periodic date/time rule.
     * Rule conflicts are solved by last-match policy.
     *
     * @author Martin Srom
     */
    public static class Rule
    {
        private RuleType type;

        private AbsoluteDateTime dateTimeFrom;

        private AbsoluteDateTime dateTimeTo;

        /**
         * Construct rule that performs it's effect for concrete date/time
         *
         * @param type          Type of rule
         * @param dateTime      Concrete date/time
         *
         */
        public Rule(RuleType type, AbsoluteDateTime dateTime)
        {
            this.type = type;
            this.dateTimeFrom = dateTime;
        }

        /**
         * Construct rule that performs it's effect for interval of date/times
         *
         * @param type            Type of rule
         * @param dateTimeFrom    Start of date/time interval
         * @param dateTimeTo      End of date/time interval
         */
        public Rule(RuleType type, AbsoluteDateTime dateTimeFrom, AbsoluteDateTime dateTimeTo)
        {
            this.type = type;
            this.dateTimeFrom = dateTimeFrom;
            this.dateTimeTo = dateTimeTo;
        }
    }
}

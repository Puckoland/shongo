#
# Abstract reservation request
#
# @author Martin Srom <martin.srom@cesnet.cz>
#
package Shongo::ClientCli::API::ReservationRequestNormal;
use base qw(Shongo::ClientCli::API::ReservationRequestAbstract);

use strict;
use warnings;

use Shongo::Common;
use Shongo::Console;

# Enumeration of reservation request purpose
our $Purpose = ordered_hash('SCIENCE' => 'Science', 'EDUCATION' => 'Education');

#
# Create a new instance of reservation request
#
# @static
#
sub new()
{
    my $class = shift;
    my (%attributes) = @_;
    my $self = Shongo::ClientCli::API::ReservationRequestAbstract->new(@_);
    bless $self, $class;

    $self->add_attribute('purpose', {
        'type' => 'enum',
        'enum' => $Purpose,
        'required' => 1
    });
    $self->add_attribute('specification', {
        'complex' => 1,
        'modify' => sub {
            my ($specification) = @_;
            my $class = undef;
            if ( defined($specification) ) {
                $class = $specification->{'class'};
            }
            $class = Shongo::ClientCli::API::Specification::select_type($class);
            if ( !defined($specification) || !($class eq $specification->get_object_class()) ) {
                $specification = Shongo::ClientCli::API::Specification->create({'class' => $class});
            } else {
                $specification->modify();
            }
            return $specification;
        },
        'required' => 1
    });
    $self->add_attribute('providedReservationIds', {
        'title' => 'Provided reservations',
        'type' => 'collection',
        'item' => {
            'title' => 'provided reservation',
            'add' => sub {
                return console_edit_value("Reservation identifier", 1, $Shongo::Common::IdPattern);
            },
            'format' => sub {
                my ($providedReservationId) = @_;
                return sprintf("identifier: %s", $providedReservationId);
            }
        },
        'complex' => 0
    });
    return $self;
}

1;
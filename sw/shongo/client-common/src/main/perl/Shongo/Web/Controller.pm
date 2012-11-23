#
# Controller.
#
# @author Martin Srom <martin.srom@cesnet.cz>
#
package Shongo::Web::Controller;

use strict;
use warnings;
use Shongo::Common;

#
# Create a new instance of controller.
#
# @param $name         name of the controller (is used as location, e.g. "my-super-controller")
# @param $application  web application
# @static
#
sub new
{
    my $class = shift;
    my ($name, $application) = @_;
    my $self = {};
    bless $self, $class;

    $self->{'name'} = $name;
    $self->{'application'} = $application;

    return $self;
}

#
# @return name of the controller which is used as location
#
sub get_name
{
    my ($self) = @_;
    return $self->{'name'};
}

#
# @return base of the controller
#
sub get_location
{
    my ($self) = @_;
    return '/' . $self->get_name();
}

#
# @param $name
# @return value of param $name
#
sub get_param
{
    my ($self, $name) = @_;
    return $self->{'application'}->{'cgi'}->param($name);
}

#
# @param $name
# @return value of param $name
#
sub get_param_required
{
    my ($self, $name) = @_;
    my $value = $self->get_param($name);
    if ( !defined($value) ) {
        $self->{'application'}->error_action("Param '$name' was not present and is required.");
    }
    return $value;
}

#
# @see Shongo::Web::Application::render_page
#
sub render_page
{
    my ($self, $title, $file, $parameters) = @_;
    $parameters->{'location'} = $self->get_location();
    $self->{'application'}->render_page($title, $file, $parameters);
}

#
# @see Shongo::Web::Application::render_page_content
#
sub render_page_content
{
    my ($self, $title, $content) = @_;
    $self->{'application'}->render_page_content($title, $content);
}

#
# Redirect to given $url
#
# @param $url
#
sub redirect
{
    my ($self, $url) = @_;
    if ( !($url =~ /^\//) ) {
        $url = $self->get_location() . '/' . $url;
    }
    print $self->{'application'}->redirect($url);
}

#
# Quit application and report error
#
# @param $error
#
sub error
{
    my ($self, $error) = @_;
    $self->{'application'}->error_action($error);
}

1;
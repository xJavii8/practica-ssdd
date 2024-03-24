from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField, EmailField)
from wtforms.validators import InputRequired, Length, Email, DataRequired, Optional


class LoginForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    password = PasswordField('password', validators=[InputRequired()])
    remember_me = BooleanField('remember_me')


class RegistrationForm(FlaskForm):
    name = StringField('name', validators=[DataRequired(), Length(min=2, max=20)])
    email = EmailField('email', validators=[DataRequired(), Email()])
    password = PasswordField('password', validators=[InputRequired(), DataRequired()])


class ChangeUserForm(FlaskForm):
    name = StringField('name', validators=[DataRequired(), Length(min=2, max=20)])
    actualEmail = StringField('email', validators=[Email(), DataRequired()])
    newMail = StringField('email', validators=[Optional(), Email()])
    newPassword = PasswordField('password', validators=[Optional()])

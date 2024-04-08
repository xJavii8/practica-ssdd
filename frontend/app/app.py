from flask import Flask, render_template, send_from_directory, url_for, request, redirect, flash, jsonify, make_response, session
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os
import logging

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegistrationForm, ChangeUserForm

# Configuración del logger
logging.basicConfig(format="%(filename)s:%(lineno)d - %(levelname)s - %(message)s")
logging.getLogger().setLevel(logging.INFO)

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app)  # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'


@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)


@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == "POST":
        convNameJSON = {"convName": request.form['convName']}
        userID = str(format(current_user.id, '032x'))
        createConvPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue', json=convNameJSON)
        logging.info("CONV STATUS: " + str(createConvPOST.status_code))
        if createConvPOST.status_code == 201:
            logging.info("CONV: " + str(createConvPOST.headers))
            logging.info("JSON: " + str(createConvPOST.json()))
            session['convName'] = request.form['convName']
            session['convID'] = createConvPOST.json().get('ID')
            session['next'] = createConvPOST.json().get('nextURL')
            session['end'] = createConvPOST.json().get('endURL')
            return redirect(url_for('conversation'))
        else:
            flash('Esta conversación ya existe. Por favor, elige otro nombre', 'danger')
    return render_template('index.html', active_page='index')


@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            loginData = {"email": form.email.data, "password": form.password.data}
            loginPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/checkLogin', json=loginData)
            if loginPOST.status_code == 200:
                json = loginPOST.json()
                user = User(int(json['id'], 16), json['name'], json['email'], form.password.data.encode('utf-8'))
                users.append(user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))
            else:
                flash('Credenciales incorrectas. Vuelve a intentarlo.', 'danger')
        elif request.method == "POST" and not form.validate():
            flash('Por favor, revisa tus credenciales.', 'danger')
        return render_template('login.html', form=form, active_page='login')


@app.route('/signup', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        form = RegistrationForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            registerData = {"email": form.email.data, "name": form.name.data, "password": form.password.data}
            registerPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/register', json=registerData)
            if registerPOST.status_code == 200:
                json = registerPOST.json()
                user = User(int(json['id'], 16), json['name'], json['email'], form.password.data.encode('utf-8'))
                users.append(user)
                login_user(user)
                return redirect(url_for('index'))
            else:
                flash('Este usuario ya existe.', 'danger')
        elif request.method == "POST" and not form.validate():
            flash('Por favor, revisa tus credenciales.', 'danger')
        return render_template('signup.html', form=form, active_page='register')


@app.route('/profile', methods=['GET', 'POST'])
@login_required
def profile():
    form = ChangeUserForm(None if request.method != 'POST' else request.form)
    if request.method == "POST" and form.validate():
        changeUserInfoData = {"actualEmail": form.actualEmail.data, "newMail": form.newMail.data, "name": form.name.data, "password": form.newPassword.data}
        changeUserInfoPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/changeInfo', json=changeUserInfoData)
        if changeUserInfoPOST.status_code == 200:
            json = changeUserInfoPOST.json()
            users.remove(current_user)
            logout_user()
            user = User(int(json['id'], 16), json['name'], json['email'], form.newPassword.data.encode('utf-8'))
            users.append(user)
            login_user(user)
            flash('Credenciales actualizadas correctamente.', 'success')
            return redirect(url_for('profile'))
        else:
            flash('Ha ocurrido un error. Inténtalo de nuevo', 'danger')
    return render_template('profile.html', form=form, active_page='profile')


@app.route('/deleteUser', methods=['POST'])
@login_required
def deleteUser():
    userID = str(format(current_user.id, '032x'))
    deleteUserPOST = requests.delete(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/deleteUser/{userID}')
    if deleteUserPOST.status_code == 200:
        users.remove(current_user)
        logout_user()
        return redirect(url_for('index'))
    else:
        flash('Ha ocurrido un error. Inténtalo de nuevo', 'danger')
        return redirect(url_for('profile'))
    
@app.route('/conversation', methods=['GET', 'POST'])
@login_required
def conversation():
    userID = str(format(current_user.id, '032x'))
    convName = session['convName']
    convID = session['convID']
    if not convName:
        flash("Esta conversación no existe.", "danger")
        return redirect(url_for('index'))
    else:
        return render_template('conversation.html', active_page='conversation', convName=convName, convID=convID)

@app.route('/endConv', methods=['POST'])
@login_required
def endConversation():
    userID = str(format(current_user.id, '032x'))
    convID = request.json.get('convID')
    endConvPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue/{convID}/end')
    logging.info("STATUS CODE: " + str(endConvPOST.status_code))
    if endConvPOST.status_code == 200:
        logging.info(endConvPOST.json())
        return jsonify({"status": "ok"}), 200
    else:
        resp = make_response(jsonify({"error": "Ha ocurrido un error. Inténtalo de nuevo"}), 500)
        return resp
    
@app.route('/sendPrompt', methods=['POST'])
@login_required
def sendPrompt():
    userID = str(format(current_user.id, '032x'))
    nextURL = session['next']
    logging.info("NEXTURL: " + str(nextURL))
    logging.info(session.__str__)
    json = {'userID': userID, 'convID': session['convID'], 'prompt': request.json.get('prompt')}
    sendPromptPOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service{nextURL}', json=json)
    logging.info("STATUS CODE: " + str(sendPromptPOST.status_code))
    if sendPromptPOST.status_code == 200:
       logging.info("OK")
    return None


@app.route('/getConvData', methods=['POST'])
@login_required
def getConvData():
    userID = str(format(current_user.id, '032x'))
    convID = request.json.get('convID')
    getConvDataGET = requests.get(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue/{convID}')
    logging.info("STATUS CODE: " + str(getConvDataGET.status_code))
    if getConvDataGET.status_code == 200:
        logging.info(getConvDataGET.json())
        session['convID'] = getConvDataGET.json().get('convID')
        session['convName'] = getConvDataGET.json().get('convName')
        session['next'] = getConvDataGET.json().get('nextURL')
        session['end'] = getConvDataGET.json().get('endURL')
        return jsonify({"status": "ok"}), 200
    else:
        resp = make_response(jsonify({"error": "Ha ocurrido un error. Inténtalo de nuevo"}), 500)
        return resp


@app.route('/allConversations', methods=['GET'])
@login_required
def allConversations():
    userID = str(format(current_user.id, '032x'))
    getConvGET = requests.get(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue')
    logging.info(getConvGET.status_code)
    if getConvGET.status_code == 200:
        logging.info(getConvGET.json())
        allConvs = getConvGET.json().get('allConvs', None)
    else:
        allConvs = None
    
    return render_template('conversationsList.html', active_page='allConversations', allConvs=allConvs)


@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))


@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))

from flask import Flask, render_template, send_from_directory, url_for, request, redirect, flash, jsonify, make_response, session
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os
import logging
from datetime import datetime

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
    if request.method == "POST": # En este caso, una petición POST es para crear una nueva conversación
        requestJSON = {"convName": request.form['convName']}
        userID = str(format(current_user.id, '032x'))
        response = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue', json=requestJSON)

        if response.status_code == 201:
            responseJSON = response.json()
            session['convName'] = responseJSON.get('name') # Guardamos los datos de la conversación para usarlos en otros puntos del código
            session['convID'] = responseJSON.get('ID')
            session['next'] = responseJSON.get('nextURL')
            session['end'] = responseJSON.get('endURL')
            session['messages'] = None
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
            requestJSON = {"email": form.email.data, "password": form.password.data}
            response = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/checkLogin', json=requestJSON)

            if response.status_code == 200: # Tratamiento de los datos para el inicio de sesión del usuario
                responseJSON = response.json()
                uid = int(responseJSON['id'], 16)
                user = User(uid, responseJSON['name'], responseJSON['email'], form.password.data.encode('utf-8'))
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
            requestJSON = {"email": form.email.data, "name": form.name.data, "password": form.password.data}
            response = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/register', json=requestJSON)

            if response.status_code == 200: # Tratamiento de los datos para el registro del usuario
                responseJSON = response.json()
                user = User(int(responseJSON['id'], 16), responseJSON['name'], responseJSON['email'], form.password.data.encode('utf-8'))
                users.append(user)
                login_user(user) # En este caso, cuando el usuario se registra, inicia sesión automáticamente
                return redirect(url_for('index'))
            else:
                flash('Este usuario ya existe.', 'danger')

        elif request.method == "POST" and not form.validate():
            flash('Por favor, revisa tus credenciales.', 'danger')

        return render_template('signup.html', form=form, active_page='register')


@app.route('/profile', methods=['GET', 'POST'])
@login_required
def profile():
    userID = str(format(current_user.id, '032x'))
    form = ChangeUserForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():
        # Si se trata de una petición POST, el usuario quiere actualizar sus datos
        requestJSON = {"actualEmail": form.actualEmail.data, "newMail": form.newMail.data, "name": form.name.data, "password": form.newPassword.data}
        responsePOST = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/changeInfo', json=requestJSON)
    
        if responsePOST.status_code == 200: # Tratamiento de los datos para su actualización
            responseJSON = responsePOST.json()
            users.remove(current_user)
            logout_user()
            user = User(int(responseJSON['id'], 16), responseJSON['name'], responseJSON['email'], form.newPassword.data.encode('utf-8'))
            users.append(user)
            login_user(user)
            flash('Credenciales actualizadas correctamente.', 'success')
            return redirect(url_for('profile'))
        else:
            flash('Ha ocurrido un error. Inténtalo de nuevo', 'danger')

    # En este caso, siempre vamos a obtener las estadísticas del usuario, ya que puede que se haya creado
    # una nueva conversación o se hayan hecho más llamadas al prompt
    responseGET = requests.get(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/stats')

    if responseGET.status_code == 200:
        statsJSON = responseGET.json()
        createdConvs = statsJSON.get('createdConvs', 0)
        promptCalls = statsJSON.get('promptCalls', 0)
    else:
        createdConvs = 0
        promptCalls = 0
        flash("Ha ocurrido un error al obtener tus estadísticas. Inténtalo de nuevo", "danger")

    return render_template('profile.html', form=form, active_page='profile', createdConvs=createdConvs, promptCalls=promptCalls)


@app.route('/deleteUser', methods=['POST'])
@login_required
def deleteUser():
    userID = str(format(current_user.id, '032x'))
    response = requests.delete(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/deleteUser/{userID}')

    if response.status_code == 200:
        users.remove(current_user)
        logout_user()
        return redirect(url_for('index'))
    else:
        flash('Ha ocurrido un error. Inténtalo de nuevo', 'danger')
        return redirect(url_for('profile'))
    
@app.route('/conversation', methods=['GET', 'POST'])
@login_required
def conversation():
    convName = session['convName']
    convID = session['convID']
    dialogues = session.get('messages', None) # Es posible que se trate de una nueva conversación y no existan mensajes

    if not convName or not convID:
        flash("Esta conversación no existe.", "danger")
        return redirect(url_for('index'))
    else:
        return render_template('conversation.html', active_page='conversation', convName=convName, convID=convID, dialogues=dialogues)


@app.route('/conversationLog', methods=['GET', 'POST'])
@login_required
def conversationLog():
    convName = session['convName']
    convID = session['convID']
    dialogues = session.get('messages', None) # Es posible que se trate de una conversación en la que no existan mensajes

    if not convName or not convID:
        flash("Esta conversación no existe.", "danger")
        return redirect(url_for('index'))
    else:
        return render_template('conversationLog.html', active_page='conversationLog', convName=convName, convID=convID, dialogues=dialogues)


@app.route('/endConv', methods=['POST'])
@login_required
def endConversation():
    userID = str(format(current_user.id, '032x'))
    convID = request.json.get('convID')
    response = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue/{convID}/end')

    if response.status_code == 200:
        session.pop('convID', None) # Eliminamos todos los datos de la conversación del diccionario
        session.pop('convName', None)
        session.pop('next', None)
        session.pop('end', None)
        session.pop('messages', None)
        return jsonify({"status": "ok"}), 200
    else:
        return make_response(jsonify({"error": "Ha ocurrido un error. Inténtalo de nuevo"}), 500)
    

@app.route('/delConv', methods=['DELETE'])
@login_required
def delConversation():
    userID = str(format(current_user.id, '032x'))
    convID = request.json.get('convID')
    response = requests.delete(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue/{convID}/del')

    if response.status_code == 200:
        session.pop('convID', None) # Eliminamos todos los datos de la conversación del diccionario
        session.pop('convName', None)
        session.pop('next', None)
        session.pop('end', None)
        session.pop('messages', None)
        return jsonify({"status": "ok"}), 200
    else:
        return make_response(jsonify({"error": "Ha ocurrido un error. Inténtalo de nuevo"}), 500)
    

@app.route('/delAllConvs', methods=['DELETE'])
@login_required
def delAllConvs():
    userID = str(format(current_user.id, '032x'))
    response = requests.delete(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/delAllConvs')

    if response.status_code == 200:
        flash('Se han eliminado todas las conversaciones', 'success')
        return jsonify({"status": "ok"}), 200
    else:
        flash('No hay conversaciones para eliminar', 'danger')
        return make_response(jsonify({"error": "No hay conversaciones disponibles"}), 204)
    

@app.route('/sendPrompt', methods=['POST'])
@login_required
def sendPrompt():
    userID = str(format(current_user.id, '032x'))
    nextURL = session['next']
    timestamp = datetime.now()
    timestamp = datetime.timestamp(timestamp)
    timestamp = round(timestamp * 1000)
    requestJSON = {'userID': userID, 'convID': session['convID'], 'prompt': request.json.get('prompt'), 'timestamp': timestamp}
    response = requests.post(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service{nextURL}', json=requestJSON)

    if response.status_code == 200:
       return response.json()
    elif response.status_code == 204:
        flash("Error: esta conversación aún no está lista para mandar más mensajes", "danger")
        return make_response('', 204)


@app.route('/getConvData', methods=['POST'])
@login_required
def getConvData():
    userID = str(format(current_user.id, '032x'))
    convID = request.json.get('convID')
    response = requests.get(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue/{convID}')

    if response.status_code == 200:
        responseJSON = response.json()
        session['convID'] = responseJSON.get('convID')
        session['convName'] = responseJSON.get('convName')
        session['next'] = responseJSON.get('nextURL')
        session['end'] = responseJSON.get('endURL')
        session['messages'] = responseJSON.get('dialogues')
        return jsonify({"status": responseJSON.get('status')}), 200
    else:
        resp = make_response(jsonify({"error": "Ha ocurrido un error. Inténtalo de nuevo"}), 500)
        return resp


@app.route('/allConversations', methods=['GET'])
@login_required
def allConversations():
    userID = str(format(current_user.id, '032x'))
    response = requests.get(f'http://{os.environ.get("REST_SERVER", "backend-rest")}:8080/Service/u/{userID}/dialogue')

    if response.status_code == 200:
        allConvs = response.json().get('allConvs', None)
    else:
        allConvs = None
    
    return render_template('conversationsList.html', active_page='allConversations', allConvs=allConvs)


@app.route('/logout')
@login_required
def logout():
    # Si un usuario cierra sesión, no debe quedar información sobre otras conversaciones
    session.pop('convID', None)
    session.pop('convName', None)
    session.pop('next', None)
    session.pop('end', None)
    session.pop('messages', None)
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

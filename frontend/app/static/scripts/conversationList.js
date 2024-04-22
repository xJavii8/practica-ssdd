document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('delAllConv').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'flex'; // Diálogo de confirmación para eliminar todas las conversaciones
    });

    document.getElementById('confirmDel').addEventListener('click', function() {
        const dialogContainer = document.getElementById('confirmationDialog');

        // Solicitud backend
        fetch('/delAllConvs', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (!response.ok && response.status !== 204) {
                throw new Error('Algo salió mal al eliminar las conversaciones.');
            }

            return;
        })
        .then(data => {
            window.location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
        });

        document.getElementById('confirmationDialog').style.display = 'none';
    });

    document.getElementById('cancelDel').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'none';
    });

    document.querySelectorAll('.conversation-name').forEach(item => {
        item.addEventListener('click', () => {
            // Obtenemos la información de la conversación
            const convID = item.getAttribute('data-convID');
            fetch('/getConvData', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ convID: convID })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Algo salió mal');
                }
                return response.json();
            })
            .then(data => {
                if(data.status === 3) { // Si la conversación está terminada lo enviamos a conversationLog, sino, a conversation
                    window.location.href = '/conversationLog';
                } else {
                    window.location.href = '/conversation';
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    });
});

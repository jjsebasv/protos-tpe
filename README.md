# protos-tpe

## Test Plan
 * Descargar Prosody (Server)
 * Descargar Pidgin (Client)
 * Copiar el archivo `test.cfg.lua` en la carpeta `/etc/prosody/conf.avail` y crear la imagen en `/etc/prosody/conf.d`
 * Crear como host (en `/etc/hosts`) `protos-tpe` y direccionarlo a `127.0.0.1`
 
## Run
 * Crear un usuario en el servidor
   * Se puede hacer desde pidgin:
     * Conectar un XMPP nuevo
     * Inventar username
     * El dominio tiene que ser el lugar donde este guardado el server (`protos-tpe`)
     * Poner en `true` el campo para crear en el server
     * En `advanced` utlizar el puerto del server (`5228`) y poner `usar encriptacion si esta disponible`
     * Conectar - Va a pedir registrar, usar los mismos campos (importante agregar el `@protos-tpe` al usuario) y recordar contraseña
     * SE CREO EL USUARIO
     * Desconectar el cambiar el puerto por el puerto del proxy (`5223`)
 * Darle play al proxy (desde java)
 * Conectar el usuario al proxy y un usuario directamente al server
 * Agregar un buddy
 * Empezar a hablar
 

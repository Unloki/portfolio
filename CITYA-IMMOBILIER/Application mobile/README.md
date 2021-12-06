# PRÉSENTATION DE L'APPLICATION CITYA IMMOBILIER
# Base de l'application
App XML est le point de départ des déclarations de votre application. Visual Studio le crée automatiquement pour vous quand vous commencez une nouvelle application WPF, en créant aussi le fichier Code-behind appelé App.xaml.cs. Ils fonctionnent un peu comme une fenêtre, où les deux fichiers sont des classes partielles, travaillant ensemble pour vous permettre de travailler à la fois sur le balisage (XAML) et le code-behind.
App .XML Cs étend la classe application, qui est une classe centrale dans une application Windows WPF. Le .NET Framework ira à cette classe pour démarrer les instructions et ensuite démarrer la fenêtre voulue ou la page désirée à partir de là. C'est aussi l'endroit pour souscrire à des événements d'application importants, tels que le démarrage de l'application, les exceptions non gérées...
L'une des fonctionnalités les plus utilisées du fichier App XML est de définir des ressources globales pouvant être utilisées et accessible depuis l'ensemble d'une application, par exemple des styles globaux.

## Disposition des fichiers :
Dossier 
> Citya (le projet partagé)
> Citya.iOS (le projet iOS)
> Citya.Android (le projet Android

Dans le projet partagé, plusieurs dossiers:
> Custom

Ce dossier contient les vues des cellules utilisées dans les listes

> Data

Contient les fichiers de parametrage des bases de données sqlite ainsi que la classe DownloadData qui regroupe les fonctions liées au téléchargement depuis l'API

> Models

Contient les différentes classes du projet

> ViewModels

Contient les viewsmodels qui manipule les entités

> Views

Contient toutes les vues, le dossier helpPage contient les pages d'aides, OtherPage contient les pages liés a la page menu.

## Paquet
Les « packages NuGet » contiennent le code compilé (sous forme de DLL) ainsi que tout le contenu nécessaire aux projets qui les utilisent.
NuGet est le gestionnaire de packages le plus répandu pour le développement .NET, et il est intégré à Visual Studio pour Mac et à Visual Studio sur Windows. Vous pouvez rechercher des packages et les ajouter à vos projets Xamarin, .NET Core et ASP.NET en utilisant l’un ou l’autre de ces IDE.
Plusieurs paquets sont utilisés dans le projet :
> Xamarin.Forms.Map
> Xamarin.Firebase.Core
> Xamarin.Firebase.Messaging
> Xamarin.FFImageLoading
Plugin.Share
Plugin.Toast
Xamarin.Plugin.AnimationNavigationPage
Xam.Plugins.Settings
Xam.Plugin.Geolocator

Ces paquets sont initialisé dans les fichiers de lancement de l'application(ici MainActivity.cs pour android et AppDelegate.cs pour Ios)
Certains paquets sont initialisé dans le fichier  App.Xaml.cs

## Maps
Les maps de l'application sous Android utilise une clé API Maps, cette clé est administrable à :
https://console.cloud.google.com/google/maps-apis/
Avec le compte app.citya@gmail.com (Voir mdp sur le keyPass);
Pour IOS, Apple met à disposition une map par défault.
Bien evidement, pour l'utilisation de la géolocalitaion et les Maps, des droits sont nécessaire.
> IoS => info.plist
> Android => Manifest.xml

On initialise les maps dans les fichiers MainActivity et AppDelegate:
Xamarin.FormsMaps.Init();
pour afficher une map :
```C#
public Map customMap = new Map();
customMap = new Map
            {
                IsShowingUser = true,
                HeightRequest = 100,                 WidthRequest = 960,                 VerticalOptions = LayoutOptions.FillAndExpand,                 HorizontalOptions = LayoutOptions.FillAndExpand             };

```
Pour zoomer sur la map :
```
customMap.MoveToRegion(MapSpan.FromCenterAndRadius(new Position(latitude, longitude), Distance.FromMeters(200)));
```
Pour ajouter un pin :
```
var pin = new CustomPin
        {
            Type = PinType.Place,
            Position = position,
            Label = "Message",
            Address = "adresse"
        };

        pin.Clicked += async (object sender, EventArgs e) =>
        {
            await Navigation.PushModalAsync(new AgenceDetailPage(agence));
        };
        customMap.Pins.Add(pin);
```

### Custom Renderer
Les interfaces utilisateur Xamarin.Forms sont affichées à l’aide des contrôles natifs de la plateforme cible, ce qui permet aux applications Xamarin.Forms de conserver l’apparence appropriée pour chaque plateforme. Les convertisseurs personnalisés permettent aux développeurs de remplacer ce processus pour personnaliser l’apparence et le comportement des contrôles Xamarin.Forms sur chaque plateforme.
Le changement de l’apparence d’un contrôle Xamarin.Forms, sans renderer personnalisé, est un processus en deux étapes qui implique la création d’un contrôle personnalisé par l’intermédiaire d’une sous-classe, puis la consommation du contrôle personnalisé à la place du contrôle d’origine.

Les cutoms renderer dans l'application:
* CustomTabbedRenderer
* MyEntryRenderer

CustomTabbedRenderer permet la customisation de l'affichage de la page TabbedPage.
Le TabbedPage de Xamarin.Forms se compose d’une liste d’onglets et d’une zone de détails plus grande, chaque onglet chargeant du contenu dans la zone de détails.
Les icons sont de base affiché en glyph (une seule couleur)
Pour intégrer des icons multicolor il fallait utiliser un customRenderer

Le deuxième customRenderer: MyEntryRenderer
Ce custom renderer change les propriète du type Entry de xamarin.forms
Xamarin.Forms Entry est utilisé pour l’entrée de texte à ligne unique. Le Entry, comme le Editor afficher, prend en charge plusieurs types de clavier. En outre, le Entry peut être utilisé comme un champ de mot de passe.
Ce custom renderer modifit la couleur de fond, ajoute des effet d'ombre, rend les bords de l'entre carré ou les bords courbable.
Contrairement au CustomTabbedRenderer qui remplacer par default, celui-ci est utilisable.
On peut appeler le custom renderer de cette façon
```
 <local:MyEntry  CornerRadius="1"  BackgroundColor="White" TextColor="Gray">
                                <Entry.Keyboard>
                                    <Keyboard x:FactoryMethod="Create">
                                        <x:Arguments>
                                            <KeyboardFlags>
                                                Suggestions, CapitalizeWord  
                                            </KeyboardFlags>
                                        </x:Arguments>
                                    </Keyboard>
                                </Entry.Keyboard>
                            </local:MyEntry>    
```
On remarque l'accés au propriété  CornerRadius="1"  BackgroundColor="White"

### Les permissions :
Sur ios, la gestion des droits s’administre dans le fichier info.plist, il contient aussi l’accès aux autorisations à la caméra, au microphone, aux appareils Bluetooth de l’appareil. 
Pour Android, la gestion des autorisations se déclare dans le fichier AndroidManifest.xml, pour demander ses autorisations à l’utilisateur, une fonction dans le fichier MainActivity.cs récupère toutes les autorisations nécessaires au bon fonctionnement de l’application et les demandes. La classe Utilesscs permet la requête des permissions nécessaire. 
Cette classe utilise le plugiciel : Plugiciel. Push Notification Au début de l’application, on appelle la méthode CheckPermissionStatusAsync contenu dans cette classe, avec l’id de la permission souhaitée. Pour chaque plateforme, la méthode affiche la demande de droits à l’utilisateur. 
Si les droits sont authorisés, on modifie la valeur des booléens dans les pages utilisant la position, ainsi, les fonctions liées  

### La géolocalisation : 
Au lancement de l’application, une fois les droits demandés et autorisés, on peut récupérer la position de l’utilisateur en utilisant le paquet Plugin.Geolocator, si la position est accessible, alors on change un Boolean dans les pages Formulaire et Agence, deux pages principales de l’application. Ces pages effectuent des requêtes vers l'api avec la position du mobile
Avec ces résultats, s’ensuivent des requêtes différentes selon les pages, pour le formulaire, a récupéré la ville et on l’ajoute dans le modèle de la recherche d’annonce, cela permet à l’utilisateur de ne pas être obligé de renseigner une ville et pouvoir directement lancer la recherche. Pour la page agences, on effectue une requête de liste d’agences avec le code département, cette liste d’agences est directement affichée dans la carte, la carte est centrée sur l’agence la plus proche de l’utilisateur.

### Les Tokens
L'accès à l'API nécéssite des tokens, pour chaque requètes, un token est demandé.
Les tokens sont demandés à cette adresse : 
https://www.citya.com/authentication_token
Lors du lancement de l'application, ou lors d'un changement d'état de connection de l'appareil, on demande l'obtention d'un token au serveur.
On passe dans la requete POST le contenu suivant:
```
{
    "username": "userApplicationCitya",
    "password": "voir KeyPass"
}
```
Cet identifiant est stocké dans la BDD Citya.
Le serveur envoie en réponse le token sous forme de Json.
Pour chaque requete vers l'API, le token est inséré dans l'entête;
Example :
```
 webclient.Headers["Authorization"] = "Bearer " + Application.Current.Properties["token"].ToString();
 ```

### L'API
l'addresse de l'api : https://www.citya.com/api/ ( retourne du Json)
Pour envoyer des requêtes, on utilise les actions dans le fichier DownloadDataClass.cs
Ce fichier comporte toutes les actions qui intéragissent avec l'API
Pour chaque action :

```
//L'addresse du serveur est contenu dans la propriété IpAddressServer du fichier App.Xaml.cs
string URL = App.IpAddressServeur + "URL";
// on instancie un WebClient (package System.Net.Http)
        using (var webclient = new WebClient())
            {
                webclient.DownloadStringCompleted += (object sender, DownloadStringCompletedEventArgs e) =>
                {
                    try
                    {
                    // e.Result correspond à la reponse du serveur (Json)
                        string resultJson = e.Result;
                        String codeInsee = resultJson.ToString();
                        Device.BeginInvokeOnMainThread(() =>
                        {
                        // pour finir on invoke le resultat (List, object, string..)
                            action.Invoke(codeInsee);
                        });
                    }
                    catch (Exception ex)
                    {
                    // en cas d'echec
                        Device.BeginInvokeOnMainThread(() =>
                        {
                            action.Invoke(null);
                        });
                    }
                };
                webclient.DownloadStringAsync(new Uri(URL));
            }
```
Exemple d'utilisation d'une de ces actions:
```
// on utilise la methode DownloadDataActualiteTheme dans le fichier DownloadDataClass.cs
// on recupere en reponse une liste d'objet "actusTheme"
 Data.DownloadDataClass.DownloadDataActualiteTheme((actusTheme) =>
            {
            //Ici on affiche la liste des themes d'actualités en nourrisant la listeView ItemsListeViewTheme
                ItemsListViewTheme.ItemsSource = actusTheme;
            });
```
On nourrit la list ItemsListViewTheme avec la liste d'objet récuperé par l'action DownloadDataActualiteTheme
## ListView
Un ListView est rempli avec les données à l’aide du ItemsSource propriété (comme vu ci-dessus il peut être remplie avec des objets récupéré depuis l'API, qui peut accepter toute collection implémentant IEnumerable. La façon la plus simple pour remplir un ListView implique l’utilisation d’un tableau de chaînes.
ListView présente les listes déroulantes, chaque cellules peuvent être utilisé pour afficher du texte et des images, indiquant un état true/false et recevoir des entrées d’utilisateur.
Lorsque les cellules intégrées ne fournissent pas la disposition requise, cellules personnalisés implémenté la disposition requise. Par exemple, vous souhaiterez présenter une cellule avec deux étiquettes qui ont le même poids. Un TextCell serait insuffisant, car le TextCell a un nom qui est plus petit. La plupart des personnalisations de cellule ajouter des données en lecture seule supplémentaires (par exemple, des étiquettes supplémentaires, images ou d’autres informations d’affichage).

Toutes les cellules personnalisés doivent dériver de ViewCell , la même classe de base que tous de la cellule intégrée types utilisent.
Exemple :
```
 <ListView x:Name="ItemsListView" Margin="0,-5,0,0"   
                          Grid.Row="1"
                          IsPullToRefreshEnabled="true"
                           ItemSelected="OnItemSelected"
                          RowHeight="400"
                          ItemsSource="{Binding Annonces}"
                          >
                    <ListView.ItemTemplate>
                        <DataTemplate>
                            <ViewCell>
                                <custom:AnnonceCellView BindingContext = "{Binding}"  />
                            </ViewCell>
                        </DataTemplate>
                    </ListView.ItemTemplate>
                </ListView>  
```
La cellule personnalisée est imbriquée dans une DataTemplate, ce qui se trouve dans ListView.ItemTemplate. Ceci est le même processus qu’à l’aide de n’importe quelle autre cellule.
ViewCell est le type de la cellule personnalisée. L’enfant de la DataTemplate élément doit être un dériver du type ViewCell.
<custom:AnnonceCellView BindingContext = "{Binding}"  /> : AnnonceCellView est la View contenu dans le fichier AnnonceCellView.xaml.cs, on lie à cet vue chaque objet de la liste;
Dans cette vue on récupére l'objet de cette façon:
```
protected override void OnBindingContextChanged()
        {
            annonce = this.BindingContext as Annonce;
        }
```
Une fois l'annonce récupérer, on peut attribuer ces propriétés à des éléments graphique de la view:
```
 ville.Text = annonce.ville;
```

# Firebase Analytics et Cloud Messaging
Le projet est implementé sur Firebase
https://console.firebase.google.com/ 
avec comme identifiant : app.citya@gmail.com
mdp: voir keypass

## Plugin utilisés: 
### Pour android :
> Xamarin.Firebase.Core
> Plugin.PushNotification
> Xamarin.Firebase.Messaging
> Xamarin.Plugins.Messaging

### Pour Ios :
> Plugin.PushNotification
> Xamarin.Firebase.Core
> XamarinLFirebase.iOS.Analytics

## Firebase Analytics : 
Firebase Analytics est une solution gratuite de mesure d'application qui fournit des informations sur l'utilisation des applications et l'engagement de l'utilisateur.Firebase affiche sous forme de graphes, le nombre de connexions et d'utilisateur actifs par jour.Il envoie d'événements personnalisés sont intégrés dans le projet.Une interface est implémenté pour Android et ios, EventTrackerDroid et EventTrackerIOS, ces deux interfaces possédent 3 méthodes qui envoie des événements à Firebase avec différentes données (string, triple string, dictionnary)Pour utiliser cette interface pour envoyer des événements à Firebase :on déclare l'interface au début d'une classe .XML Cs : Readonly IEventTracker event Tracker;puis on l'initialise lors de la création de la page :Event Tracker = Dependency Service .GetIEventTracker>();ensuite on peut appeler la fonction Send Event :Event Tracker .Send Event("titre", "Recherche avec détails", "détails");firebase recevra les événements et affichera les détails, on peut ainsi connaître quelles recherches effectuent les utilisateurs ou quelles pages sont visualisées.

## Firebase Cloud Messaging :
Pour l'utilisation de cloud messaging avec une application Xamarin. Formes, son déploiement est plus complexe et nécessite l'utilisation d'un plugiciel : Plugiciel. Push NotificationCe plugin permet de faciliter la reception de notifications à distance l'initialisation pour chaque plateforme est légérement différentes : voir https://github.com/CrossGeeks/PushNotificationPlugin/blob/master/docs/GettingStarted.mdLe message de la notification est récupéré dans le fichier App .xaml.cs grace à la méthode:CrossPushNotification. Current. OnNotificationOpened, cette méthode passe en paramètrer un objet qui correspond à la notification.

## Sqlite :
Le projet utilise une base de données Sqlite, Deux bases sont utilisées: la table annonce et la table HistoriqueCes bases sont déclaré dans le fichier Annonce Data.cs et recherchehistoriquedata.cs.Elles sont initialisées dans le fichier App .xaml.csCes bases de données servent à stocker l'historique de recherche de l'utilisateur et à stocker les annonces favorites.Chaque base est déclarée représenté par une classe, les classes contiennent les méthodes pour sauvegarder l'objet, supprimer l'objet, ou récupérer l'objet(ou une liste d'objets).


## PROJET RÉFÉRENT
https://github.com/jguertl/SharePlugin
https://github.com/xamarin/xamarin-forms-samples/blob/master/GetStarted/Notes/Database/Notes/Data/NoteDatabase.cs
https://github.com/luberda-molinet/FFImageLoading/tree/master/samples
https://github.com/alexrainman/CarouselView/blob/master/Demo/MainViewModel.cs














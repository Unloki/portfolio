using EBMACH.Interface;
using EBMACH.Model;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Forms;

namespace EBMACH
{
    public partial class MainPage : ContentPage
    {
        FirebaseTasker firebaseTasker = new FirebaseTasker();
        public MainPage()
        {
            InitializeComponent();
            BindingContext = this;
        }

        protected async override void OnAppearing()
        {
            base.OnAppearing();
            var temperature = await firebaseTasker.GetTemperature();
            BeerLabel.Text = temperature.beer.ToString()+"°";
            RoomLabel.Text = temperature.room.ToString() + "°";
            if (checkIsToDay(temperature.DateFormatObject))
            {
                DateTime date1 = DateTime.Now;
                DateTime date2 = new DateTime(temperature.DateFormatObject.Year, temperature.DateFormatObject.Month, temperature.DateFormatObject.Day, temperature.DateFormatObject.Hour, temperature.DateFormatObject.Min, date1.Second);
                TimeSpan ts = date1 - date2;
                TimeLabel.Text = "Il y a " + ts.Minutes + "m";
            }
            else
            {
                TimeLabel.Text = "Il y a ∞";
            }
        }
        private bool checkIsToDay(DateFormat dateFormat)
        {
            DateTime today = DateTime.Today;
            if (dateFormat.Day == today.Day && dateFormat.Month == today.Month && dateFormat.Year == today.Year)
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        private async void BtnAdd_Clicked(object sender, EventArgs e)
        {
            await firebaseTasker.AddOrder("On");
            var OnOfTemp = await firebaseTasker.GetStateMachine();
            if (OnOfTemp.state == "On")
            {
                await DisplayAlert("Parfait !", "Activation de la machine", "OK");
            }
            else
            {
                await DisplayAlert("Parfait !", "Activation de la machine", "OK");
            }
        }
        public async void OnPickerSelectedIndexChanged(object sender, EventArgs e)
        {
            Picker picker = sender as Picker;
            var selectedItem = picker.SelectedItem;
            DependencyService.Get<IMessage>().ShortAlert("Affichage en " + selectedItem);
        }
    }
}

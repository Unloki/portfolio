using Firebase.Database;
using Firebase.Database.Query;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Collections.Generic;
using System;

namespace EBMACH.Model
{
    class FirebaseTasker
    {
        FirebaseClient firebase = new FirebaseClient("https://beer-tap-esp32-default-rtdb.europe-west1.firebasedatabase.app/");

        //Get the last temperature insert in the firebase database
        public async Task<Temperature> GetTemperature()
        {
            var items = (await firebase
              .Child("ESP32/temperature")
              .OnceAsync<Temperature>()).Select(item => new Temperature
              {
                  beer = item.Object.beer,
                  room = item.Object.room,
                  date = item.Object.date
              }).ToList();
            Temperature response = items.FirstOrDefault();
            if (response != null)
            {
                response.SetDate();
                return response;
            }
            else return new Temperature { beer = 0, room = 0 };
        }
        public async Task<OnOF> GetStateMachine()
        {
            var items = (await firebase
              .Child("ESP32/order")
              .OnceAsync<OnOF>()).Select(item => new OnOF
              {
                  state = item.Object.state,
                  date = item.Object.date
              }).ToList();
            OnOF response = items.FirstOrDefault();
            if (response != null)
            {
                response.SetDate();
                return response;
            }
            else
                return new OnOF { state = "Of" };
        }
        public async Task<List<Energie>> GetWattHourDay(String the_day, String the_month, String the_year)
        {
            var items = (await firebase
              .Child("ESP32/energie")
              .OnceAsync<Energie>()).Select(item => new Energie
              {
                  date = item.Object.date,
                  watt = item.Object.watt
              }).ToList();
            return new List<Energie>(items.FindAll(item => item.DateFormatObject.Day == Int32.Parse(the_day)
                                                           && item.DateFormatObject.Month == Int32.Parse(the_month)
                                                           && item.DateFormatObject.Year == Int32.Parse(the_year)));
        }
        public async Task<List<Energie>> GetWattHourWeek(String the_day, String the_month, String the_year)
        {
            var items = (await firebase
              .Child("ESP32/energie")
              .OnceAsync<Energie>()).Select(item => new Energie
              {
                  date = item.Object.date,
                  watt = item.Object.watt
              }).ToList();
            //Creation of a new object DateTime with the parameter of the function, this object will be use for get all day of his week
            DateTime selectDay = new DateTime(Int32.Parse(the_year), Int32.Parse(the_month), Int32.Parse(the_day));
            int currentDayOfWeek = (int)selectDay.DayOfWeek;
            DateTime sunday = selectDay.AddDays(-currentDayOfWeek);
            DateTime monday = sunday.AddDays(1);
            if (currentDayOfWeek == 0) monday = monday.AddDays(-7);
            var dates = Enumerable.Range(0, 7).Select(days => monday.AddDays(days)).ToList();
            var resultItems = new List<Energie>();
            //for each day of the week, we extract the day in the list from firebase
            foreach(DateTime date in dates)
            {
                resultItems.Add(items.Find(item => item.DateFormatObject.Day == date.Day
                                                           && item.DateFormatObject.Month == Int32.Parse(the_month)
                                                           && item.DateFormatObject.Year == Int32.Parse(the_year)));
            }
            return resultItems;
        }
        public async Task<List<Energie>> GetWattHourMonth(String the_day, String the_month, String the_year)
        {
            var items = (await firebase
              .Child("ESP32/energie")
              .OnceAsync<Energie>()).Select(item => new Energie
              {
                  date = item.Object.date,
                  watt = item.Object.watt
              }).ToList();
            return new List<Energie>(items.FindAll(item => item.DateFormatObject.Month == Int32.Parse(the_month)
                                                           && item.DateFormatObject.Year == Int32.Parse(the_year)));
        }

        public async Task AddOrder(String state)
        {
            //TODO delete order existant
            await firebase
              .Child("ESP32/order")
              .PostAsync(new OnOF() { state = state, date = "15-11-2021|20:10:25" });
        }
    }
}

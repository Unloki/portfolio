using System;
using System.Collections.Generic;
using System.Text;

namespace EBMACH.Model
{
    class Energie
    {
        public string date { get; set; }

        public float watt { get; set; }

        public void SetDate()
        {
            this.DateFormatObject = new DateFormat(this.date);
        }
        public DateFormat DateFormatObject { get; set; }

    }
}
